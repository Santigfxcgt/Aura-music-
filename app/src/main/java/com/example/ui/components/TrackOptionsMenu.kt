package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.theme.AuraAccent
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.LosslessGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TrackOptionsMenu(
    track: Track,
    expanded: Boolean,
    isFavorite: Boolean,
    onDismissRequest: () -> Unit,
    onPlayNow: () -> Unit,
    onAddToQueue: () -> Unit,
    onViewLyrics: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditTrack: () -> Unit = {},
    onDeletePermanently: () -> Unit = {}
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .background(Color(0xF5161720))
            .border(0.5.dp, GlassCardBorder, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .testTag("track_options_dropdown_${track.id}")
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = track.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )
            Text(
                text = "${track.artist} • ${track.qualityFormat}",
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1
            )
        }

        MenuItem(
            text = "Reproducir ahora",
            icon = Icons.Filled.PlayArrow,
            tint = Color.White,
            onClick = {
                onDismissRequest()
                onPlayNow()
            }
        )

        MenuItem(
            text = "Añadir a la cola",
            icon = Icons.Filled.PlaylistAdd,
            tint = Color.White.copy(alpha = 0.9f),
            onClick = {
                onDismissRequest()
                onAddToQueue()
            }
        )

        MenuItem(
            text = "Editar info y portada animada",
            icon = Icons.Filled.Edit,
            tint = LosslessGold,
            onClick = {
                onDismissRequest()
                onEditTrack()
            }
        )

        MenuItem(
            text = "Ver letras",
            icon = Icons.Filled.Subtitles,
            tint = Color.White.copy(alpha = 0.9f),
            onClick = {
                onDismissRequest()
                onViewLyrics()
            }
        )

        MenuItem(
            text = if (isFavorite) "Quitar de favoritas" else "Guardar en favoritas",
            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            tint = AuraAccent,
            onClick = {
                onDismissRequest()
                onToggleFavorite()
            }
        )

        MenuItem(
            text = "Eliminar permanentemente",
            icon = Icons.Filled.DeleteForever,
            tint = Color(0xFFEF4444),
            onClick = {
                onDismissRequest()
                onDeletePermanently()
            }
        )
    }
}

@Composable
private fun MenuItem(
    text: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    color = if (tint == Color(0xFFEF4444)) Color(0xFFEF4444) else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        onClick = onClick
    )
}
