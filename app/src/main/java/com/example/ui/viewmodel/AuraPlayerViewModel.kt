package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PlaylistEntity
import com.example.data.model.Track
import com.example.data.repository.LocalAudioScannerRepository
import com.example.data.repository.PlaylistRepository
import com.example.data.repository.TrackRepository
import com.example.player.Media3PlayerManager
import com.example.ui.components.VisualizerType
import com.example.util.ScrapedMetadataResult
import com.example.util.SmartMetadataScraper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab {
    HOME,
    SONGS,
    LIBRARY
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlayerUiState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = 0,
    val selectedTab: NavigationTab = NavigationTab.HOME,
    val isNowPlayingExpanded: Boolean = false,
    val isQueueSheetVisible: Boolean = false,
    val isLyricsVisible: Boolean = false,
    val isSettingsDialogVisible: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val favoriteTrackIds: Set<String> = setOf("t_mi_vicio", "t1", "t3"),
    val audioQuality: String = "Hi-Res Lossless (24-bit/96kHz)",
    val soundEnhancement: String = "Studio Spatial Aura",
    val localTracks: List<Track> = emptyList(),
    val isScanningLocalAudio: Boolean = false,
    val localAudioScannedCount: Int = 0,
    val hasStoragePermission: Boolean = false,
    val selectedTrackFilter: String = "ALL", // "ALL", "LOCAL", "BUNDLED"
    val editingTrack: Track? = null,
    val playlists: List<PlaylistEntity> = emptyList(),
    val visualizerType: VisualizerType = VisualizerType.WAVEFORM_BARS,
    val crossfadeSec: Int = 3,
    val isGapless: Boolean = true,
    val isEqualizerDialogVisible: Boolean = false,
    val isEqualizerEnabled: Boolean = true,
    val eqBands: List<Float> = listOf(6f, 5f, 3f, 0f, -2f, 2f, 4f, 5f, 6f, 7f),
    val bassBoostPercent: Int = 60,
    val virtualizerPercent: Int = 40,
    val reverbPreset: String = "OFF",
    val selectedEqPreset: String = "EDM / Electrónica",
    val deletingTrackConfirm: Track? = null
)

class AuraPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val playerManager = Media3PlayerManager(application)
    private val localScanner = LocalAudioScannerRepository(application)
    private val customMetadataRepo = com.example.data.repository.CustomMetadataRepository(application)
    private val playlistRepo = PlaylistRepository(application)

    private var rawBundledTracks: List<Track> = TrackRepository.getTracks()
    private var rawLocalTracks: List<Track> = emptyList()
    private var customMetadataMap: Map<String, com.example.data.local.CustomTrackMetadata> = emptyMap()

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            currentTrack = TrackRepository.getTracks().firstOrNull(),
            queue = TrackRepository.getTracks(),
            queueIndex = 0,
            durationMs = TrackRepository.getTracks().firstOrNull()?.durationMs ?: 30000L
        )
    )

    val uiState: StateFlow<PlayerUiState> = combine(
        _uiState,
        playerManager.isPlaying,
        playerManager.currentPositionMs,
        playerManager.durationMs
    ) { baseState, isPlaying, pos, dur ->
        baseState.copy(
            isPlaying = isPlaying,
            currentPositionMs = pos,
            durationMs = if (dur > 0L) dur else (baseState.currentTrack?.durationMs ?: 30000L)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        _uiState.value
    )

    init {
        playerManager.onTrackCompleted = {
            handleTrackCompleted()
        }
        playerManager.onSkipToNext = {
            playNext()
        }
        playerManager.onSkipToPrevious = {
            playPrevious()
        }

        viewModelScope.launch {
            customMetadataRepo.allCustomMetadata.collect { metadataList ->
                customMetadataMap = metadataList.associateBy { it.trackId }
                refreshTracksWithCustomMetadata()
            }
        }

        viewModelScope.launch {
            playlistRepo.allPlaylists.collect { list ->
                _uiState.value = _uiState.value.copy(playlists = list)
            }
        }
    }

    private fun refreshTracksWithCustomMetadata() {
        val meta = customMetadataMap
        val mergedBundled = rawBundledTracks.map { it.withCustomMetadata(meta[it.id]) }
        val mergedLocal = rawLocalTracks.map { it.withCustomMetadata(meta[it.id]) }
        val combined = (mergedLocal + mergedBundled).distinctBy { it.id }

        val current = _uiState.value.currentTrack?.let { curr ->
            combined.firstOrNull { it.id == curr.id } ?: curr.withCustomMetadata(meta[curr.id])
        } ?: combined.firstOrNull()

        _uiState.value = _uiState.value.copy(
            localTracks = mergedLocal,
            queue = combined,
            currentTrack = current,
            editingTrack = _uiState.value.editingTrack?.let { ed ->
                combined.firstOrNull { it.id == ed.id } ?: ed.withCustomMetadata(meta[ed.id])
            }
        )
    }

    private fun Track.withCustomMetadata(meta: com.example.data.local.CustomTrackMetadata?): Track {
        return if (meta != null) {
            copy(
                title = meta.customTitle?.takeIf { it.isNotBlank() } ?: this.title,
                artist = meta.customArtist?.takeIf { it.isNotBlank() } ?: this.artist,
                album = meta.customAlbum?.takeIf { it.isNotBlank() } ?: this.album,
                customMediaUri = meta.customMediaUri,
                customMediaType = meta.mediaType
            )
        } else {
            copy(
                customMediaUri = null,
                customMediaType = null
            )
        }
    }

    fun setStoragePermission(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasStoragePermission = granted)
        // No automatic scan upon entering the app or granting permission
    }

    fun scanLocalAudio() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanningLocalAudio = true)
            val scanned = localScanner.scanLocalAudioFiles()
            rawLocalTracks = scanned
            _uiState.value = _uiState.value.copy(
                isScanningLocalAudio = false,
                localAudioScannedCount = scanned.size
            )
            refreshTracksWithCustomMetadata()
        }
    }

    fun scanFolder(treeUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanningLocalAudio = true)
            val scanned = localScanner.scanFolderFiles(treeUri)
            rawLocalTracks = scanned
            _uiState.value = _uiState.value.copy(
                isScanningLocalAudio = false,
                localAudioScannedCount = scanned.size
            )
            refreshTracksWithCustomMetadata()
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                playlistRepo.createPlaylist(name.trim())
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistRepo.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            playlistRepo.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            playlistRepo.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun cycleVisualizerType() {
        val nextType = when (_uiState.value.visualizerType) {
            VisualizerType.WAVEFORM_BARS -> VisualizerType.RADIAL_SPECTRUM
            VisualizerType.RADIAL_SPECTRUM -> VisualizerType.LIQUID_WAVE
            VisualizerType.LIQUID_WAVE -> VisualizerType.WAVEFORM_BARS
        }
        _uiState.value = _uiState.value.copy(visualizerType = nextType)
    }

    fun setCrossfadeSec(seconds: Int) {
        _uiState.value = _uiState.value.copy(crossfadeSec = seconds)
        playerManager.setCrossfadeDurationSec(seconds)
    }

    fun setGaplessEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isGapless = enabled)
        playerManager.setGaplessEnabled(enabled)
    }

    fun setEqualizerDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isEqualizerDialogVisible = visible)
    }

    fun toggleEqualizerEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isEqualizerEnabled = enabled)
        playerManager.setEqEnabled(enabled)
    }

    fun setEqBandGain(bandIndex: Int, gainDb: Float) {
        val current = _uiState.value.eqBands.toMutableList()
        if (bandIndex in current.indices) {
            current[bandIndex] = gainDb
            _uiState.value = _uiState.value.copy(
                eqBands = current,
                selectedEqPreset = "Personalizado"
            )
            playerManager.setEqBandGain(bandIndex, gainDb)
        }
    }

    fun setBassBoostPercent(percent: Int) {
        val p = percent.coerceIn(0, 100)
        _uiState.value = _uiState.value.copy(bassBoostPercent = p)
        playerManager.setBassBoostPercent(p)
    }

    fun setVirtualizerPercent(percent: Int) {
        val p = percent.coerceIn(0, 100)
        _uiState.value = _uiState.value.copy(virtualizerPercent = p)
        playerManager.setVirtualizerPercent(p)
    }

    fun setReverbPreset(preset: String) {
        _uiState.value = _uiState.value.copy(reverbPreset = preset)
        playerManager.setReverbPreset(preset)
    }

    fun applyEqPreset(presetName: String) {
        val gains = com.example.ui.screens.EQ_PRESETS_MAP[presetName]
        if (gains != null) {
            _uiState.value = _uiState.value.copy(
                eqBands = gains,
                selectedEqPreset = presetName
            )
            gains.forEachIndexed { index, gain ->
                playerManager.setEqBandGain(index, gain)
            }
        }
    }

    fun resetEqToFlat() {
        applyEqPreset("Plano")
        setBassBoostPercent(0)
        setVirtualizerPercent(0)
        setReverbPreset("OFF")
    }

    fun autoEnhanceTrack(
        track: Track,
        onComplete: (ScrapedMetadataResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = SmartMetadataScraper.scrapeMetadata(
                rawTitle = track.title,
                rawArtist = track.artist,
                rawAlbum = track.album
            )
            onComplete(result)
        }
    }

    fun setTrackFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedTrackFilter = filter)
    }

    private fun handleTrackCompleted() {
        val state = _uiState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                state.currentTrack?.let {
                    playerManager.playTrack(it, 0L)
                }
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                if (state.queueIndex < state.queue.size - 1) {
                    playNext()
                } else {
                    playerManager.pause()
                }
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun playTrack(track: Track) {
        val queue = _uiState.value.queue
        val index = queue.indexOfFirst { it.id == track.id }.let { if (it >= 0) it else 0 }
        _uiState.value = _uiState.value.copy(
            currentTrack = track,
            queueIndex = index
        )
        playerManager.playTrack(track, 0L)
    }

    fun togglePlayPause() {
        val state = _uiState.value
        val track = state.currentTrack ?: return
        if (state.isPlaying) {
            playerManager.pause()
        } else {
            if (playerManager.durationMs.value == 0L) {
                playerManager.playTrack(track, state.currentPositionMs)
            } else {
                playerManager.togglePlayPause()
            }
        }
    }

    fun playNext() {
        val state = _uiState.value
        if (state.queue.isEmpty()) return
        val nextIndex = if (state.isShuffle) {
            val available = state.queue.indices.filter { it != state.queueIndex }
            if (available.isNotEmpty()) available.random() else 0
        } else {
            (state.queueIndex + 1) % state.queue.size
        }
        val nextTrack = state.queue[nextIndex]
        _uiState.value = state.copy(
            currentTrack = nextTrack,
            queueIndex = nextIndex
        )
        playerManager.playTrack(nextTrack, 0L)
    }

    fun playPrevious() {
        val state = _uiState.value
        if (playerManager.currentPositionMs.value > 3000L) {
            playerManager.seekTo(0L)
            return
        }
        if (state.queue.isEmpty()) return
        val prevIndex = if (state.queueIndex - 1 < 0) state.queue.size - 1 else state.queueIndex - 1
        val prevTrack = state.queue[prevIndex]
        _uiState.value = state.copy(
            currentTrack = prevTrack,
            queueIndex = prevIndex
        )
        playerManager.playTrack(prevTrack, 0L)
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun toggleShuffle() {
        _uiState.value = _uiState.value.copy(isShuffle = !_uiState.value.isShuffle)
    }

    fun cycleRepeatMode() {
        val nextMode = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _uiState.value = _uiState.value.copy(repeatMode = nextMode)
    }

    fun toggleFavorite(trackId: String) {
        val currentFavs = _uiState.value.favoriteTrackIds.toMutableSet()
        if (currentFavs.contains(trackId)) {
            currentFavs.remove(trackId)
        } else {
            currentFavs.add(trackId)
        }
        _uiState.value = _uiState.value.copy(favoriteTrackIds = currentFavs)
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _uiState.value = _uiState.value.copy(isNowPlayingExpanded = expanded)
    }

    fun setQueueSheetVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isQueueSheetVisible = visible)
    }

    fun setLyricsVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isLyricsVisible = visible)
    }

    fun setSettingsDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isSettingsDialogVisible = visible)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSearchActive(active: Boolean) {
        _uiState.value = _uiState.value.copy(
            isSearchActive = active,
            searchQuery = if (!active) "" else _uiState.value.searchQuery
        )
    }

    fun addToQueue(track: Track) {
        val updated = _uiState.value.queue + track
        _uiState.value = _uiState.value.copy(queue = updated)
    }

    fun removeFromQueue(index: Int) {
        val current = _uiState.value.queue.toMutableList()
        if (index in current.indices && current.size > 1) {
            current.removeAt(index)
            val newIdx = _uiState.value.queueIndex.coerceAtMost(current.size - 1)
            _uiState.value = _uiState.value.copy(queue = current, queueIndex = newIdx)
        }
    }

    fun setAudioQuality(quality: String) {
        _uiState.value = _uiState.value.copy(audioQuality = quality)
    }

    fun setSoundEnhancement(enhancement: String) {
        _uiState.value = _uiState.value.copy(soundEnhancement = enhancement)
    }

    fun setEditingTrack(track: Track?) {
        _uiState.value = _uiState.value.copy(editingTrack = track)
    }

    fun saveCustomTrack(
        trackId: String,
        title: String,
        artist: String,
        album: String,
        mediaUri: android.net.Uri?
    ) {
        viewModelScope.launch {
            customMetadataRepo.saveTrackMetadata(
                trackId = trackId,
                title = title,
                artist = artist,
                album = album,
                mediaUri = mediaUri
            )
            refreshTracksWithCustomMetadata()
        }
    }

    fun resetCustomTrack(trackId: String) {
        viewModelScope.launch {
            customMetadataRepo.resetToOriginal(trackId)
            refreshTracksWithCustomMetadata()
        }
    }

    fun requestDeleteTrackPermanently(track: Track) {
        _uiState.value = _uiState.value.copy(deletingTrackConfirm = track)
    }

    fun cancelDeleteTrack() {
        _uiState.value = _uiState.value.copy(deletingTrackConfirm = null)
    }

    fun confirmDeleteTrackPermanently() {
        val track = _uiState.value.deletingTrackConfirm ?: return
        viewModelScope.launch {
            if (!track.contentUriString.isNullOrBlank()) {
                try {
                    val uri = android.net.Uri.parse(track.contentUriString)
                    if (track.contentUriString.startsWith("content://")) {
                        getApplication<Application>().contentResolver.delete(uri, null, null)
                    } else if (track.contentUriString.startsWith("file://")) {
                        val path = uri.path
                        if (!path.isNullOrBlank()) {
                            java.io.File(path).delete()
                        }
                    } else {
                        java.io.File(track.contentUriString).delete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            customMetadataRepo.resetToOriginal(track.id)

            rawLocalTracks = rawLocalTracks.filter { it.id != track.id }
            rawBundledTracks = rawBundledTracks.filter { it.id != track.id }

            val updatedQueue = _uiState.value.queue.filter { it.id != track.id }
            val isCurrentTrackDeleted = _uiState.value.currentTrack?.id == track.id

            if (isCurrentTrackDeleted) {
                if (updatedQueue.isNotEmpty()) {
                    val nextTrack = updatedQueue.first()
                    _uiState.value = _uiState.value.copy(
                        queue = updatedQueue,
                        currentTrack = nextTrack,
                        queueIndex = 0,
                        deletingTrackConfirm = null
                    )
                    playerManager.playTrack(nextTrack, 0L)
                } else {
                    playerManager.pause()
                    _uiState.value = _uiState.value.copy(
                        queue = emptyList(),
                        currentTrack = null,
                        queueIndex = 0,
                        deletingTrackConfirm = null
                    )
                }
            } else {
                val newIndex = updatedQueue.indexOfFirst { it.id == _uiState.value.currentTrack?.id }.coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(
                    queue = updatedQueue,
                    queueIndex = newIndex,
                    deletingTrackConfirm = null
                )
            }

            refreshTracksWithCustomMetadata()
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
