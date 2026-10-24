package com.example.ui.screens

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.ui.components.GlassBox
import com.example.ui.components.TrackArtworkImage
import com.example.ui.theme.AuraAccent
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.LosslessGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AuraImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTrackDialog(
    track: Track,
    onDismissRequest: () -> Unit,
    onSave: (title: String, artist: String, album: String, mediaUri: Uri?) -> Unit,
    onResetOriginal: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf(track.title) }
    var artist by remember { mutableStateOf(track.artist) }
    var album by remember { mutableStateOf(track.album) }

    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var mediaTypeLabel by remember {
        mutableStateOf(track.customMediaType ?: if (track.isLocalTrack) "Local" else "Catálogo")
    }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isValidating by remember { mutableStateOf(false) }

    // System Media Picker for Images, GIFs, and Videos
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        isValidating = true
        validationError = null

        coroutineScope.launch {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val isVideo = mimeType.startsWith("video/") ||
                    uri.toString().lowercase().let { it.endsWith(".mp4") || it.endsWith(".mkv") || it.endsWith(".webm") }
            val isGif = mimeType.contains("gif", ignoreCase = true) ||
                    uri.toString().lowercase().endsWith(".gif")

            if (isVideo) {
                withContext(Dispatchers.IO) {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, uri)
                        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        val durationMs = durationStr?.toLongOrNull() ?: 0L

                        if (durationMs > 10500L) {
                            val durationSec = durationMs / 1000.0
                            validationError = "El vídeo dura %.1f s. El límite máximo para portadas animadas es de 10 segundos.".format(durationSec)
                        } else {
                            selectedMediaUri = uri
                            mediaTypeLabel = "VÍDEO CLIP (%.1f s)".format(durationMs / 1000.0)
                        }
                    } catch (e: Exception) {
                        validationError = "No se pudo validar el vídeo: ${e.message}"
                    } finally {
                        try {
                            retriever.release()
                        } catch (ignored: Exception) {}
                    }
                }
            } else if (isGif) {
                selectedMediaUri = uri
                mediaTypeLabel = "GIF ANIMADO"
            } else {
                selectedMediaUri = uri
                mediaTypeLabel = "IMAGEN"
            }
            isValidating = false
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag("edit_track_dialog")
    ) {
        GlassBox(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            backgroundColor = GlassSurfaceElevated,
            borderColor = GlassCardBorder,
            borderWidth = 1.dp,
            elevation = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = AuraAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Editar Pista",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Media Cover Selection Area
                Text(
                    text = "Portada Personalizada (Foto, GIF o Vídeo máx 10s)",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            1.dp,
                            GlassCardBorder,
                            RoundedCornerShape(18.dp)
                        )
                        .clickable {
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        }
                        .testTag("edit_cover_picker_area"),
                    contentAlignment = Alignment.Center
                ) {
                    // Preview existing cover or newly picked media
                    if (selectedMediaUri != null) {
                        AsyncImage(
                            model = selectedMediaUri,
                            imageLoader = AuraImageLoader.get(context),
                            contentDescription = "Nueva portada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        TrackArtworkImage(
                            track = track,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }

                    // Translucent overlay scrim with instructions
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0x77000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isValidating) {
                                CircularProgressIndicator(
                                    color = AuraAccent,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Validando duración...",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x99000000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = AuraAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Toca para cambiar portada",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Soporta JPG, PNG, GIF y Vídeo (<= 10s)",
                                    color = Color(0xCCFFFFFF),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Media Type Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when {
                                    mediaTypeLabel.contains("VÍDEO", true) -> Icons.Filled.Videocam
                                    mediaTypeLabel.contains("GIF", true) -> Icons.Filled.Gif
                                    else -> Icons.Filled.AddPhotoAlternate
                                },
                                contentDescription = null,
                                tint = LosslessGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = mediaTypeLabel,
                                color = LosslessGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Error validation message if video exceeds 10 seconds
                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33FA2D55))
                            .border(1.dp, AuraAccent, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = AuraAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = validationError!!,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Auto-Enhance Smart Metadata Scraper Button
                var isScraping by remember { mutableStateOf(false) }
                var scraperSuccessMsg by remember { mutableStateOf<String?>(null) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                            )
                        )
                        .clickable(enabled = !isScraping) {
                            coroutineScope.launch {
                                isScraping = true
                                scraperSuccessMsg = null
                                val result = com.example.util.SmartMetadataScraper.scrapeMetadata(title, artist, album)
                                title = result.cleanTitle
                                artist = result.cleanArtist
                                album = result.cleanAlbum
                                if (!result.hdCoverUrl.isNullOrBlank()) {
                                    selectedMediaUri = Uri.parse(result.hdCoverUrl)
                                    mediaTypeLabel = "CARÁTULA HD"
                                }
                                isScraping = false
                                scraperSuccessMsg = "✨ ¡Metadatos e información HD actualizados!"
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .testTag("auto_enhance_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isScraping) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buscando metadatos y letras HD...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✨ Auto-Mejorar Metadatos & Carátula HD",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (scraperSuccessMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = scraperSuccessMsg!!,
                        color = LosslessGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Editable Fields: Title, Artist, Album
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la canción") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_title_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraAccent,
                        unfocusedBorderColor = GlassCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AuraAccent,
                        unfocusedLabelColor = TextMuted
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artista") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_artist_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraAccent,
                        unfocusedBorderColor = GlassCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AuraAccent,
                        unfocusedLabelColor = TextMuted
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Álbum") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_album_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraAccent,
                        unfocusedBorderColor = GlassCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AuraAccent,
                        unfocusedLabelColor = TextMuted
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons: Reset to original & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (track.customMediaUri != null || track.title != title) {
                        OutlinedButton(
                            onClick = {
                                onResetOriginal()
                                onDismissRequest()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("reset_track_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassCardBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Restablecer",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (validationError == null) {
                                onSave(title, artist, album, selectedMediaUri)
                                onDismissRequest()
                            }
                        },
                        enabled = !isValidating && validationError == null,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_track_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraAccent,
                            disabledContainerColor = AuraAccent.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Guardar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
