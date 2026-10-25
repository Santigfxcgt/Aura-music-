package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.AuraAccent
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.LosslessGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.RepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    track: Track,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    isFavorite: Boolean,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onCollapse)

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragValue by remember { mutableFloatStateOf(0f) }

    val trackDuration = if (durationMs > 0) durationMs else track.durationMs
    val currentPosition = if (isDraggingSlider) (sliderDragValue * trackDuration).toLong() else currentPositionMs
    val sliderPosition = if (trackDuration > 0) {
        if (isDraggingSlider) sliderDragValue else (currentPosition.toFloat() / trackDuration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val albumScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.92f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "albumScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .testTag("now_playing_screen")
    ) {
        // 1. Blurred Backdrop Based on Song Artwork (Apple Music Liquid Blur)
        Box(modifier = Modifier.fillMaxSize()) {
            if (track.customMediaType == "VIDEO" && !track.customMediaUri.isNullOrBlank()) {
                com.example.ui.components.LoopingVideoArtwork(
                    videoPathOrUri = track.customMediaUri,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(65.dp)
                        .scale(1.4f)
                )
            } else {
                TrackArtworkImage(
                    track = track,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(65.dp)
                        .scale(1.4f)
                )
            }

            // Apple Music Liquid Dark Vignette Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xD90A0A0E),
                                Color(0xB812121A),
                                Color(0xF208080C)
                            )
                        )
                    )
            )
        }

        // 2. Main Player Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Collapse Icon, Audio Quality Badge, and Favorite Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.testTag("now_playing_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Ocultar reproductor",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Apple Music-style Frosted Audio Quality Badge (Clickable for 10-Band EQ)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x33FFFFFF))
                        .border(
                            0.5.dp,
                            GlassCardBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onOpenEqualizer() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("audio_quality_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Equalizer,
                            contentDescription = "Abrir Ecualizador",
                            tint = if (track.isLossless) LosslessGold else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${track.qualityFormat} • EQ",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("now_playing_favorite_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Quitar de favoritas" else "Guardar en favoritas",
                        tint = if (isFavorite) AuraAccent else TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Centralized Hero Album Art with Apple Music Natural Elevation & Radius
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .scale(albumScale)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = Color(0x66000000),
                        spotColor = Color(0x88000000)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .border(
                        0.5.dp,
                        Color(0x33FFFFFF),
                        RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Real-Time Animated Audio Visualizer background
                com.example.ui.components.AudioVisualizer(
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    modifier = Modifier.fillMaxSize()
                )

                if (track.customMediaType == "VIDEO" && !track.customMediaUri.isNullOrBlank()) {
                    com.example.ui.components.LoopingVideoArtwork(
                        videoPathOrUri = track.customMediaUri,
                        modifier = Modifier.fillMaxSize(),
                        clipShape = RoundedCornerShape(22.dp)
                    )
                } else {
                    TrackArtworkImage(
                        track = track,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title and Artist with Apple Music Clean Typography
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = track.title,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.artist,
                        color = TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FFFFFF))
                            .border(0.5.dp, Color(0x1FFFFFFF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (track.isLocalTrack) "Local • ${track.codec}" else track.album,
                            color = Color(0xFFE2E4EC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Scrubber Bar & Timestamps
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        isDraggingSlider = true
                        sliderDragValue = it
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        onSeekTo((sliderDragValue * trackDuration).toLong())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("now_playing_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    val remaining = (trackDuration - currentPosition).coerceAtLeast(0L)
                    Text(
                        text = "-${formatTime(remaining)}",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5-Button Inline Playback Controls Row (Matching Reference Layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("now_playing_controls_container"),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Shuffle Button
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("now_playing_shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Modo aleatorio",
                        tint = if (isShuffle) AuraAccent else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 2. Previous Track Button
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("now_playing_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Pista anterior",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 3. Central Play / Pause Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onPlayPause)
                        .testTag("now_playing_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = Color(0xFF101014),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // 4. Next Track Button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("now_playing_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Siguiente pista",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 5. Repeat Button
                IconButton(
                    onClick = onCycleRepeat,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("now_playing_repeat_button")
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.ONE -> Icons.Filled.RepeatOne
                            else -> Icons.Filled.Repeat
                        },
                        contentDescription = "Modo repetir",
                        tint = if (repeatMode != RepeatMode.OFF) AuraAccent else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Row Capsules: Ecualizador, Cola de reproducción, Letras
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Capsule: "Ecualizador"
                GlassBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(onClick = onOpenEqualizer)
                        .testTag("now_playing_eq_button"),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color(0x33FFFFFF),
                    borderColor = GlassCardBorder,
                    borderWidth = 0.5.dp,
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Equalizer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ecualizador",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Right Capsule: "Cola de reproducción"
                GlassBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(onClick = onOpenQueue)
                        .testTag("now_playing_queue_button"),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color(0x33FFFFFF),
                    borderColor = GlassCardBorder,
                    borderWidth = 0.5.dp,
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cola de reproducción",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Round Letras Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .border(0.5.dp, GlassCardBorder, CircleShape)
                        .clickable(onClick = onOpenLyrics)
                        .testTag("now_playing_lyrics_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Letras",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
