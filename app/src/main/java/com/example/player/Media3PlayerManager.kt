package com.example.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionResult
import com.example.MainActivity
import com.example.data.model.Track
import com.example.util.NotificationArtworkHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Media3PlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var artworkJob: Job? = null

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
    var onSkipToNext: (() -> Unit)? = null
    var onSkipToPrevious: (() -> Unit)? = null

    fun setCrossfadeDurationSec(seconds: Int) {
        _crossfadeSec.value = seconds.coerceIn(0, 12)
    }

    fun setGaplessEnabled(enabled: Boolean) {
        _isGapless.value = enabled
    }

    private val nativeEqHelper = com.example.util.NativeEqualizerHelper()

    fun setEqEnabled(enabled: Boolean) {
        nativeEqHelper.setEnabled(enabled)
    }

    fun setEqBandGain(bandIndex: Int, gainDb: Float) {
        nativeEqHelper.setBandGain(bandIndex, gainDb)
    }

    fun setBassBoostPercent(percent: Int) {
        nativeEqHelper.setBassBoostStrength(percent)
    }

    fun setVirtualizerPercent(percent: Int) {
        nativeEqHelper.setVirtualizerStrength(percent)
    }

    fun setReverbPreset(preset: String) {
        nativeEqHelper.setReverbPreset(preset)
    }

    init {
        initPlayer()
    }

    @OptIn(UnstableApi::class)
    private fun initPlayer() {
        if (exoPlayer != null) return

        val player = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        exoPlayer = player

        try {
            nativeEqHelper.attachToAudioSession(player.audioSessionId)
        } catch (ignored: Exception) {}

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val sessionCallback = object : MediaSession.Callback {
            override fun onPlayerCommandRequest(
                session: MediaSession,
                controllerInfo: MediaSession.ControllerInfo,
                playerCommand: Int
            ): Int {
                if (playerCommand == Player.COMMAND_SEEK_TO_NEXT) {
                    onSkipToNext?.invoke()
                    return SessionResult.RESULT_SUCCESS
                }
                if (playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS) {
                    onSkipToPrevious?.invoke()
                    return SessionResult.RESULT_SUCCESS
                }
                return super.onPlayerCommandRequest(session, controllerInfo, playerCommand)
            }
        }

        mediaSession = MediaSession.Builder(context, player)
            .setSessionActivity(pendingIntent)
            .setCallback(sessionCallback)
            .build().also {
                AuraPlaybackService.activeMediaSession = it
            }

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
                notifyPlaybackService(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val dur = player.duration
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

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                error.printStackTrace()
                _isPlaying.value = false
                stopProgressTracker()
                try {
                    onTrackCompleted?.invoke()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun notifyPlaybackService(
        title: String? = null,
        artist: String? = null,
        artworkBytes: ByteArray? = null,
        isPlaying: Boolean = _isPlaying.value
    ) {
        try {
            val intent = Intent(context, AuraPlaybackService::class.java).apply {
                action = AuraPlaybackService.ACTION_UPDATE_NOTIFICATION
                if (!title.isNullOrBlank()) putExtra("title", title)
                if (!artist.isNullOrBlank()) putExtra("artist", artist)
                if (artworkBytes != null) putExtra("artwork", artworkBytes)
                putExtra("isPlaying", isPlaying)
            }
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(UnstableApi::class)
    fun playTrack(track: Track, startOffsetMs: Long = 0L) {
        initPlayer()
        val player = exoPlayer ?: return

        artworkJob?.cancel()

        try {
            val mediaUri = when {
                !track.contentUriString.isNullOrBlank() -> Uri.parse(track.contentUriString)
                track.audioResId != 0 -> RawResourceDataSource.buildRawResourceUri(track.audioResId)
                else -> return
            }

            val initialMetadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setIsPlayable(true)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(mediaUri)
                .setMediaId(track.id)
                .setMediaMetadata(initialMetadata)
                .build()

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

            player.volume = 1.0f
            player.play()

            notifyPlaybackService(
                title = track.title,
                artist = "${track.artist} • ${track.album}",
                artworkBytes = null,
                isPlaying = true
            )

            artworkJob = scope.launch {
                val artworkBytes = NotificationArtworkHelper.extractArtworkByteArray(context, track)
                if (artworkBytes != null && artworkBytes.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        val richMetadata = MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist)
                            .setAlbumTitle(track.album)
                            .setIsPlayable(true)
                            .setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                            .build()

                        val updatedMediaItem = MediaItem.Builder()
                            .setUri(mediaUri)
                            .setMediaId(track.id)
                            .setMediaMetadata(richMetadata)
                            .build()

                        player.replaceMediaItem(0, updatedMediaItem)

                        notifyPlaybackService(
                            title = track.title,
                            artist = "${track.artist} • ${track.album}",
                            artworkBytes = artworkBytes,
                            isPlaying = true
                        )
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
            try {
                onTrackCompleted?.invoke()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
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
        artworkJob?.cancel()
        nativeEqHelper.release()
        AuraPlaybackService.activeMediaSession = null
        mediaSession?.release()
        mediaSession = null
        exoPlayer?.release()
        exoPlayer = null
        _isPlaying.value = false
    }
}
