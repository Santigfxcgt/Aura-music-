package com.example.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class Media3PlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _crossfadeSec = MutableStateFlow(3)
    val crossfadeSec: StateFlow<Int> = _crossfadeSec.asStateFlow()

    private val _isGapless = MutableStateFlow(true)
    val isGapless: StateFlow<Boolean> = _isGapless.asStateFlow()

    var onTrackCompleted: (() -> Unit)? = null

    fun setCrossfadeDurationSec(seconds: Int) {
        _crossfadeSec.value = seconds.coerceIn(0, 12)
    }

    fun setGaplessEnabled(enabled: Boolean) {
        _isGapless.value = enabled
    }

    init {
        initPlayer()
    }

    @OptIn(UnstableApi::class)
    private fun initPlayer() {
        if (exoPlayer != null) return

        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                        if (isPlaying) {
                            startProgressTracker()
                        } else {
                            stopProgressTracker()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                val dur = duration
                                if (dur > 0L) {
                                    _durationMs.value = dur
                                }
                            }
                            Player.STATE_ENDED -> {
                                _isPlaying.value = false
                                _currentPositionMs.value = _durationMs.value
                                onTrackCompleted?.invoke()
                            }
                            Player.STATE_IDLE, Player.STATE_BUFFERING -> {}
                        }
                    }
                })
            }
    }

    @OptIn(UnstableApi::class)
    fun playTrack(track: Track, startOffsetMs: Long = 0L) {
        initPlayer()
        val player = exoPlayer ?: return

        try {
            val mediaItem = when {
                !track.contentUriString.isNullOrBlank() -> {
                    MediaItem.fromUri(Uri.parse(track.contentUriString))
                }
                track.audioResId != 0 -> {
                    val rawUri = RawResourceDataSource.buildRawResourceUri(track.audioResId)
                    MediaItem.fromUri(rawUri)
                }
                else -> return
            }

            player.setMediaItem(mediaItem)
            player.prepare()

            val estimatedDuration = if (track.durationMs > 0) track.durationMs else 30000L
            _durationMs.value = estimatedDuration

            if (startOffsetMs > 0) {
                player.seekTo(startOffsetMs)
                _currentPositionMs.value = startOffsetMs
            } else {
                _currentPositionMs.value = 0L
            }

            // Reset volume to 1f when starting track
            player.volume = 1.0f
            player.play()
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        val target = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(1L))
        player.seekTo(target)
        _currentPositionMs.value = target
        player.volume = 1.0f
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    _currentPositionMs.value = pos
                    val dur = player.duration
                    if (dur > 0L) {
                        _durationMs.value = dur

                        // Apply smooth crossfade volume fade out at track end
                        val crossfadeMs = _crossfadeSec.value * 1000L
                        val remainingMs = dur - pos
                        if (crossfadeMs > 0 && remainingMs in 1..crossfadeMs) {
                            val volumeFactor = (remainingMs.toFloat() / crossfadeMs.toFloat()).coerceIn(0.1f, 1.0f)
                            player.volume = volumeFactor
                        } else {
                            player.volume = 1.0f
                        }
                    }
                }
                delay(100L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        exoPlayer?.release()
        exoPlayer = null
        _isPlaying.value = false
    }
}
