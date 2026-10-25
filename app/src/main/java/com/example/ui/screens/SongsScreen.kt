package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.components.TrackOptionsMenu
import com.example.ui.theme.AuraAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PlayerUiState

@Composable
fun SongsScreen(
    uiState: PlayerUiState,
    onTrackSelected: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onViewLyrics: (Track) -> Unit,
    onRequestScan: () -> Unit = {},
    onFilterChange: (String) -> Unit = {},
    onEditTrack: (Track) -> Unit = {},
    onDeleteTrack: (Track) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displayTracks = remember(uiState.queue, uiState.selectedTrackFilter) {
        when (uiState.selectedTrackFilter) {
            "LOCAL" -> uiState.queue.filter { it.isLocalTrack }
            "BUNDLED" -> uiState.queue.filter { !it.isLocalTrack }
            else -> uiState.queue
        }
    }

    var selectedTrackForMenu by remember { mutableStateOf<Track?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("songs_screen")
    ) {
        // Screen Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Todas las Canciones",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Formatos soportados: FLAC, WAV, MP3, Opus, EAC3, OGG, M4A",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiState.isScanningLocalAudio) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AuraAccent,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onRequestScan,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Escanear almacenamiento",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${displayTracks.size} pistas",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Filter chips (Todas, Locales, Catálogo)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedTrackFilter == "ALL",
                onClick = { onFilterChange("ALL") },
                label = { Text("Todas (${uiState.queue.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.White,
                    selectedLabelColor = Color(0xFF101014),
                    containerColor = Color(0x1FFFFFFF),
                    labelColor = TextSecondary
                )
            )
            FilterChip(
                selected = uiState.selectedTrackFilter == "LOCAL",
                onClick = { onFilterChange("LOCAL") },
                label = { Text("Locales (${uiState.localTracks.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.White,
                    selectedLabelColor = Color(0xFF101014),
                    containerColor = Color(0x1FFFFFFF),
                    labelColor = TextSecondary
                )
            )
            FilterChip(
                selected = uiState.selectedTrackFilter == "BUNDLED",
                onClick = { onFilterChange("BUNDLED") },
                label = { Text("Catálogo Hi-Res", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.White,
                    selectedLabelColor = Color(0xFF101014),
                    containerColor = Color(0x1FFFFFFF),
                    labelColor = TextSecondary
                )
            )
        }

        // Full Vertical Track List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(displayTracks, key = { it.id }) { track ->
                val isCurrent = uiState.currentTrack?.id == track.id
                val isCurrentPlaying = isCurrent && uiState.isPlaying

                SongItemCard(
                    track = track,
                    isCurrent = isCurrent,
                    isPlaying = isCurrentPlaying,
                    onClick = { onTrackSelected(track) },
                    onMoreClick = { selectedTrackForMenu = track }
                )
            }
        }
    }

    // Context Menu for Selected Track
    selectedTrackForMenu?.let { track ->
        TrackOptionsMenu(
            track = track,
            expanded = true,
            isFavorite = uiState.favoriteTrackIds.contains(track.id),
            onDismissRequest = { selectedTrackForMenu = null },
            onPlayNow = { onTrackSelected(track) },
            onAddToQueue = { onAddToQueue(track) },
            onViewLyrics = { onViewLyrics(track) },
            onToggleFavorite = { onToggleFavorite(track.id) },
            onEditTrack = { onEditTrack(track) },
            onDeletePermanently = { onDeleteTrack(track) }
        )
    }
}
