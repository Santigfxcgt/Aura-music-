package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlaylistEntity
import com.example.data.model.Track
import com.example.data.repository.TrackRepository
import com.example.ui.components.GlassBox
import com.example.ui.theme.AuraAccent
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.LosslessGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PlayerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    uiState: PlayerUiState,
    onTrackSelected: (Track) -> Unit,
    onCreatePlaylist: (String) -> Unit = {},
    onDeletePlaylist: (String) -> Unit = {},
    onRequestScan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allTracks = remember { TrackRepository.getTracks() }
    val favoriteTracks = remember(uiState.favoriteTrackIds) {
        allTracks.filter { uiState.favoriteTrackIds.contains(it.id) }
    }

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var selectedPlaylistForDetail by remember { mutableStateOf<PlaylistEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("library_screen")
    ) {
        // Screen Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tu Biblioteca",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Colección personal, playlists y archivos locales",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Quick Scan Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x22FFFFFF))
                    .border(0.5.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                    .clickable { onRequestScan() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.isScanningLocalAudio) {
                        CircularProgressIndicator(
                            color = AuraAccent,
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = LosslessGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.isScanningLocalAudio) "Escaneando..." else "Escanear",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Stat Card in Liquid Glass
            item {
                GlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0x38191A24),
                    borderColor = GlassCardBorder,
                    elevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(
                            number = "${favoriteTracks.size}",
                            label = "Favoritas",
                            color = AuraAccent
                        )
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(0.5.dp)
                                .background(Color(0x22FFFFFF))
                        )
                        StatItem(
                            number = "${uiState.playlists.size}",
                            label = "Playlists",
                            color = LosslessGold
                        )
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(0.5.dp)
                                .background(Color(0x22FFFFFF))
                        )
                        StatItem(
                            number = "${uiState.localTracks.size}",
                            label = "Locales",
                            color = Color.White
                        )
                    }
                }
            }

            // Section: Custom Playlists
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.QueueMusic,
                            contentDescription = null,
                            tint = LosslessGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Playlists Personalizadas",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuraAccent)
                            .clickable { showCreatePlaylistDialog = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Nueva Lista",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (uiState.playlists.isEmpty()) {
                item {
                    GlassBox(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color(0x1F181923),
                        borderColor = GlassCardBorder,
                        borderWidth = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlaylistAdd,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No tienes playlists creadas aún",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Crea tu primera lista de reproducción personalizada",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(uiState.playlists) { playlist ->
                    PlaylistItemCard(
                        title = playlist.name,
                        subtitle = "Lista de reproducción personalizada",
                        icon = Icons.Filled.QueueMusic,
                        iconBg = Color(0x338B5CF6),
                        iconTint = LosslessGold,
                        onClick = {
                            selectedPlaylistForDetail = playlist
                        },
                        onDelete = {
                            onDeletePlaylist(playlist.id)
                        }
                    )
                }
            }

            // Playlist Item 1: Favoritas
            item {
                PlaylistItemCard(
                    title = "Canciones Favoritas",
                    subtitle = "${favoriteTracks.size} pistas guardadas",
                    icon = Icons.Filled.Favorite,
                    iconBg = Color(0x33FA2D55),
                    iconTint = AuraAccent,
                    onClick = {
                        favoriteTracks.firstOrNull()?.let { onTrackSelected(it) }
                    }
                )
            }

            // Playlist Item 2: Recientes / Locales
            item {
                PlaylistItemCard(
                    title = "Música Local en Dispositivo",
                    subtitle = "${uiState.localTracks.size} canciones escaneadas",
                    icon = Icons.Filled.FolderSpecial,
                    iconBg = Color(0x2EFFFFFF),
                    iconTint = Color.White,
                    onClick = {
                        uiState.localTracks.firstOrNull()?.let { onTrackSelected(it) }
                    }
                )
            }

            // Playlist Item 3: Descargas Offline
            item {
                PlaylistItemCard(
                    title = "Catálogo Hi-Res Studio",
                    subtitle = "${allTracks.size} pistas en formato Lossless",
                    icon = Icons.Filled.DownloadDone,
                    iconBg = Color(0x26E5C07B),
                    iconTint = LosslessGold,
                    onClick = {
                        allTracks.firstOrNull()?.let { onTrackSelected(it) }
                    }
                )
            }

            // Section: Pistas favoritas si existen
            if (favoriteTracks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tus favoritas seleccionadas",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(favoriteTracks.size) { index ->
                    val track = favoriteTracks[index]
                    SongItemCard(
                        track = track,
                        isCurrent = uiState.currentTrack?.id == track.id,
                        isPlaying = uiState.isPlaying && uiState.currentTrack?.id == track.id,
                        onClick = { onTrackSelected(track) },
                        onMoreClick = { /* Context menu */ }
                    )
                }
            }
        }
    }

    // Modal Dialog: Crear Playlist
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }

        BasicAlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            modifier = Modifier.testTag("create_playlist_dialog")
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
                        text = "Crear Nueva Playlist",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Nombre de la lista") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraAccent,
                            unfocusedBorderColor = GlassCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showCreatePlaylistDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text("Cancelar", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (playlistName.isNotBlank()) {
                                    onCreatePlaylist(playlistName.trim())
                                    showCreatePlaylistDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AuraAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Crear", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Playlist Details
    selectedPlaylistForDetail?.let { playlist ->
        BasicAlertDialog(
            onDismissRequest = { selectedPlaylistForDetail = null },
            modifier = Modifier.testTag("playlist_detail_dialog")
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.QueueMusic,
                                contentDescription = null,
                                tint = LosslessGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = playlist.name,
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { selectedPlaylistForDetail = null }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Selecciona canciones desde la lista principal y tócalas para añadirlas a '${playlist.name}'.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            allTracks.firstOrNull()?.let { onTrackSelected(it) }
                            selectedPlaylistForDetail = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AuraAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reproducir Toda la Lista", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    number: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlaylistItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color(0x2E181923),
        borderColor = GlassCardBorder,
        borderWidth = 1.dp,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg)
                    .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Eliminar playlist",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x1FFFFFFF))
                    .border(0.5.dp, Color(0x14FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
