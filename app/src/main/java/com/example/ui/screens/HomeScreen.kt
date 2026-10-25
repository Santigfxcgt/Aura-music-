package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.components.GlassBox
import com.example.ui.components.TrackArtworkImage
import com.example.ui.components.TrackOptionsMenu
import com.example.ui.theme.AuraAccent
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.LosslessGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PlayerUiState

@Composable
fun HomeScreen(
    uiState: PlayerUiState,
    onTrackSelected: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onViewLyrics: (Track) -> Unit,
    onOpenSettings: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onRequestScanLocalAudio: () -> Unit,
    onFilterChange: (String) -> Unit,
    onEditTrack: (Track) -> Unit = {},
    onDeleteTrack: (Track) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val quickPicks = remember(uiState.queue) {
        uiState.queue.take(5)
    }

    val displayTracks = remember(uiState.queue, uiState.selectedTrackFilter, uiState.searchQuery) {
        val filteredBySource = when (uiState.selectedTrackFilter) {
            "LOCAL" -> uiState.queue.filter { it.isLocalTrack }
            "BUNDLED" -> uiState.queue.filter { !it.isLocalTrack }
            else -> uiState.queue
        }
        if (uiState.searchQuery.isBlank()) {
            filteredBySource
        } else {
            val q = uiState.searchQuery.trim().lowercase()
            filteredBySource.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q) ||
                it.codec.lowercase().contains(q)
            }
        }
    }

    var selectedTrackForMenu by remember { mutableStateOf<Track?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top Bar: Apple Music-style Title & Search + Settings Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .border(0.5.dp, GlassCardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = AuraAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Aura Music",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Audio Hi-Res • Liquid Glass",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Top-right Icons: Search and Settings
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleSearch,
                    modifier = Modifier.testTag("top_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar canciones",
                        tint = if (uiState.isSearchActive) AuraAccent else TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Ajustes",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Expandable Search Bar
        AnimatedVisibility(
            visible = uiState.isSearchActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Buscar pistas, artistas, álbumes, codecs...", color = TextMuted) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("search_text_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassCardBorder,
                    unfocusedBorderColor = Color(0x1AFFFFFF),
                    focusedContainerColor = Color(0x2EFFFFFF),
                    unfocusedContainerColor = Color(0x1AFFFFFF),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda", tint = TextMuted)
                        }
                    }
                }
            )
        }

        // Scrollable Home Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Local Audio Scanner & Permission Banner (Liquid Glass Card)
            item {
                GlassBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("local_audio_scanner_card"),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0x38191A24),
                    borderColor = GlassCardBorder,
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x26FFFFFF))
                                        .border(0.5.dp, GlassCardBorder, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FolderOpen,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Audio Local (MediaStore)",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (uiState.isScanningLocalAudio) {
                                            "Escaneando pistas y extrayendo metadatos..."
                                        } else if (uiState.localTracks.isNotEmpty()) {
                                            "${uiState.localTracks.size} archivos locales (FLAC, MP3, WAV, Opus)"
                                        } else {
                                            "Descubrir pistas locales en tu dispositivo"
                                        },
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (uiState.isScanningLocalAudio) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = AuraAccent,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Button(
                                    onClick = onRequestScanLocalAudio,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (uiState.hasStoragePermission) Color(0x33FFFFFF) else AuraAccent
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("scan_audio_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (uiState.hasStoragePermission) Icons.Filled.Refresh else Icons.Filled.AudioFile,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (uiState.hasStoragePermission) "Escanear" else "Permiso",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Filter chips if local tracks exist
                        if (uiState.localTracks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                        }
                    }
                }
            }

            // Section 1: "Selecciones rápidas" (Quick Picks Carousel)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Selecciones rápidas",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Álbumes y pistas recomendadas para ti",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x22FFFFFF))
                                .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Lossless Master",
                                color = LosslessGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickPicks, key = { it.id }) { track ->
                            QuickPickCard(
                                track = track,
                                isCurrent = uiState.currentTrack?.id == track.id,
                                isPlaying = uiState.isPlaying && uiState.currentTrack?.id == track.id,
                                onClick = { onTrackSelected(track) }
                            )
                        }
                    }
                }
            }

            // Section 2: "Canciones" (Songs Vertical List)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Canciones",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) "Resultados de búsqueda" else "Catálogo disponible con decodificación Media3",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "${displayTracks.size} pistas",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

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

@Composable
fun QuickPickCard(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    GlassBox(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
            .testTag("quick_pick_card_${track.id}"),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (isCurrent) Color(0x52232532) else Color(0x38181923),
        borderColor = if (isCurrent) GlassCardBorder else Color(0x1AFFFFFF),
        borderWidth = 1.dp,
        elevation = if (isCurrent) 10.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Square Artwork (140x140)
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
            ) {
                TrackArtworkImage(
                    track = track,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Codec / Category Tag
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xB3000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (track.isLocalTrack) track.codec else track.category,
                        color = if (track.isLossless) LosslessGold else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Apple Music Tactile Play Button Overlay
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.BottomEnd)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.GraphicEq else Icons.Filled.PlayArrow,
                        contentDescription = "Reproducir",
                        tint = Color(0xFF101014),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = track.title,
                color = if (isCurrent) AuraAccent else TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = track.artist,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SongItemCard(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val durationMinSec = remember(track.durationMs) {
        val totalSec = track.durationMs / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        "%02d:%02d".format(min, sec)
    }

    GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("song_item_${track.id}"),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = if (isCurrent) Color(0x4D252736) else Color(0x2E181923),
        borderColor = if (isCurrent) GlassCardBorder else Color(0x14FFFFFF),
        borderWidth = 1.dp,
        elevation = if (isCurrent) 6.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with playing overlay
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
            ) {
                TrackArtworkImage(
                    track = track,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x88000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.GraphicEq else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title, Artist, Lossless Tag
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (isCurrent) AuraAccent else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artist,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = durationMinSec,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Quality Badge / Codec Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0x22FFFFFF))
                    .border(0.5.dp, Color(0x14FFFFFF), RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (track.isLocalTrack) track.codec else "Lossless",
                    color = if (track.isLossless) LosslessGold else Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Three dots options button
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("song_options_button_${track.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Opciones",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
