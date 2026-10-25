package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FloatingBottomBar
import com.example.ui.components.MiniPlayer
import com.example.ui.components.QueueBottomSheet
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TimeSyncedLyricsSheet
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.AuraPlayerViewModel
import com.example.ui.viewmodel.NavigationTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AuraPlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Android version-aware audio permission: READ_MEDIA_AUDIO (API 33+) or READ_EXTERNAL_STORAGE (API <= 32)
    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setStoragePermission(isGranted)
    }

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            requiredPermission
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setStoragePermission(isGranted)
    }

    val triggerScanAction = {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            requiredPermission
        ) == PackageManager.PERMISSION_GRANTED
        if (isGranted) {
            viewModel.scanLocalAudio()
        } else {
            permissionLauncher.launch(requiredPermission)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // Tab Content with smooth cross-fade animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            AnimatedContent(
                targetState = uiState.selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tabContent"
            ) { tab ->
                when (tab) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            uiState = uiState,
                            onTrackSelected = { viewModel.playTrack(it) },
                            onAddToQueue = { viewModel.addToQueue(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onViewLyrics = {
                                viewModel.playTrack(it)
                                viewModel.setLyricsVisible(true)
                            },
                            onOpenSettings = { viewModel.setSettingsDialogVisible(true) },
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onToggleSearch = { viewModel.setSearchActive(!uiState.isSearchActive) },
                            onRequestScanLocalAudio = triggerScanAction,
                            onFilterChange = { viewModel.setTrackFilter(it) },
                            onEditTrack = { viewModel.setEditingTrack(it) }
                        )
                    }

                    NavigationTab.SONGS -> {
                        SongsScreen(
                            uiState = uiState,
                            onTrackSelected = { viewModel.playTrack(it) },
                            onAddToQueue = { viewModel.addToQueue(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onViewLyrics = {
                                viewModel.playTrack(it)
                                viewModel.setLyricsVisible(true)
                            },
                            onRequestScan = triggerScanAction,
                            onFilterChange = { viewModel.setTrackFilter(it) },
                            onEditTrack = { viewModel.setEditingTrack(it) }
                        )
                    }

                    NavigationTab.LIBRARY -> {
                        LibraryScreen(
                            uiState = uiState,
                            onTrackSelected = { viewModel.playTrack(it) },
                            onCreatePlaylist = { viewModel.createPlaylist(it) },
                            onDeletePlaylist = { viewModel.deletePlaylist(it) },
                            onRequestScan = triggerScanAction
                        )
                    }
                }
            }
        }

        // Persistent Mini-Player and Floating Bottom Bar Dock
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Persistent Mini-Player (Right above the floating bottom bar)
            MiniPlayer(
                currentTrack = uiState.currentTrack,
                isPlaying = uiState.isPlaying,
                currentPositionMs = uiState.currentPositionMs,
                durationMs = uiState.durationMs,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onNextClick = { viewModel.playNext() },
                onExpand = { viewModel.setNowPlayingExpanded(true) }
            )

            // Floating Bottom Bar with Rounded Edges
            FloatingBottomBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }

        // Full-screen Now Playing Overlay with Slide Animation
        AnimatedVisibility(
            visible = uiState.isNowPlayingExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            uiState.currentTrack?.let { track ->
                NowPlayingScreen(
                    track = track,
                    isPlaying = uiState.isPlaying,
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    isShuffle = uiState.isShuffle,
                    repeatMode = uiState.repeatMode,
                    isFavorite = uiState.favoriteTrackIds.contains(track.id),
                    onCollapse = { viewModel.setNowPlayingExpanded(false) },
                    onPlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.playNext() },
                    onPrevious = { viewModel.playPrevious() },
                    onSeekTo = { viewModel.seekTo(it) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeatMode() },
                    onToggleFavorite = { viewModel.toggleFavorite(track.id) },
                    onOpenQueue = { viewModel.setQueueSheetVisible(true) },
                    onOpenLyrics = { viewModel.setLyricsVisible(true) },
                    onOpenEqualizer = { viewModel.setEqualizerDialogVisible(true) }
                )
            }
        }

        // Up Next ("A continuación") Queue Sheet
        if (uiState.isQueueSheetVisible) {
            QueueBottomSheet(
                queue = uiState.queue,
                currentIndex = uiState.queueIndex,
                isPlaying = uiState.isPlaying,
                onDismissRequest = { viewModel.setQueueSheetVisible(false) },
                onTrackSelected = {
                    viewModel.playTrack(it)
                    viewModel.setQueueSheetVisible(false)
                },
                onRemoveTrack = { viewModel.removeFromQueue(it) }
            )
        }

        // Time-Synced Lyrics Sheet
        if (uiState.isLyricsVisible) {
            uiState.currentTrack?.let { track ->
                TimeSyncedLyricsSheet(
                    track = track,
                    currentPositionMs = uiState.currentPositionMs,
                    onSeekTo = { viewModel.seekTo(it) },
                    onDismissRequest = { viewModel.setLyricsVisible(false) }
                )
            }
        }

        // Settings Dialog
        if (uiState.isSettingsDialogVisible) {
            SettingsDialog(
                currentQuality = uiState.audioQuality,
                onQualitySelected = { viewModel.setAudioQuality(it) },
                crossfadeSec = uiState.crossfadeSec,
                onCrossfadeChanged = { viewModel.setCrossfadeSec(it) },
                isGapless = uiState.isGapless,
                onGaplessChanged = { viewModel.setGaplessEnabled(it) },
                onOpenProEqualizer = { viewModel.setEqualizerDialogVisible(true) },
                onDismissRequest = { viewModel.setSettingsDialogVisible(false) }
            )
        }

        // Pro 10-Band Equalizer Dialog
        if (uiState.isEqualizerDialogVisible) {
            com.example.ui.screens.EqualizerDialog(
                isEnabled = uiState.isEqualizerEnabled,
                onToggleEnabled = { viewModel.toggleEqualizerEnabled(it) },
                eqBands = uiState.eqBands,
                onBandChange = { bandIndex, gainDb -> viewModel.setEqBandGain(bandIndex, gainDb) },
                bassBoostPercent = uiState.bassBoostPercent,
                onBassBoostChange = { viewModel.setBassBoostPercent(it) },
                virtualizerPercent = uiState.virtualizerPercent,
                onVirtualizerChange = { viewModel.setVirtualizerPercent(it) },
                reverbPreset = uiState.reverbPreset,
                onReverbPresetChange = { viewModel.setReverbPreset(it) },
                selectedPresetName = uiState.selectedEqPreset,
                onSelectPreset = { viewModel.applyEqPreset(it) },
                onResetEq = { viewModel.resetEqToFlat() },
                onDismissRequest = { viewModel.setEqualizerDialogVisible(false) }
            )
        }

        // Edit Track Dialog (Custom Metadata, GIF & Video Artwork)
        uiState.editingTrack?.let { trackToEdit ->
            EditTrackDialog(
                track = trackToEdit,
                onDismissRequest = { viewModel.setEditingTrack(null) },
                onSave = { title, artist, album, mediaUri ->
                    viewModel.saveCustomTrack(
                        trackId = trackToEdit.id,
                        title = title,
                        artist = artist,
                        album = album,
                        mediaUri = mediaUri
                    )
                },
                onResetOriginal = {
                    viewModel.resetCustomTrack(trackToEdit.id)
                }
            )
        }
    }
}
