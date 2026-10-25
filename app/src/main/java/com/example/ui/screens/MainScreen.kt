package com.example.ui.screens

import android.Manifest
import android.content.Intent
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassBox
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (ignored: Exception) {}
            viewModel.scanFolder(uri)
        }
    }

    val triggerFolderSelectAction = {
        folderPickerLauncher.launch(null)
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
                            onRequestScanLocalAudio = triggerFolderSelectAction,
                            onFilterChange = { viewModel.setTrackFilter(it) },
                            onEditTrack = { viewModel.setEditingTrack(it) },
                            onDeleteTrack = { viewModel.requestDeleteTrackPermanently(it) }
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
                            onRequestScan = triggerFolderSelectAction,
                            onFilterChange = { viewModel.setTrackFilter(it) },
                            onEditTrack = { viewModel.setEditingTrack(it) },
                            onDeleteTrack = { viewModel.requestDeleteTrackPermanently(it) }
                        )
                    }

                    NavigationTab.LIBRARY -> {
                        LibraryScreen(
                            uiState = uiState,
                            onTrackSelected = { viewModel.playTrack(it) },
                            onCreatePlaylist = { viewModel.createPlaylist(it) },
                            onDeletePlaylist = { viewModel.deletePlaylist(it) },
                            onRequestScan = triggerFolderSelectAction
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

        // Permanent Track Deletion Confirmation Dialog
        uiState.deletingTrackConfirm?.let { trackToDelete ->
            BasicAlertDialog(
                onDismissRequest = { viewModel.cancelDeleteTrack() }
            ) {
                GlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = GlassSurfaceElevated,
                    borderColor = GlassCardBorder,
                    borderWidth = 1.dp,
                    elevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Text(
                            text = "¿Eliminar canción permanentemente?",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Se eliminará el archivo \"${trackToDelete.title}\" de tu dispositivo. Esta acción no se puede deshacer.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { viewModel.cancelDeleteTrack() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Text("Cancelar", color = TextMuted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.confirmDeleteTrackPermanently() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
