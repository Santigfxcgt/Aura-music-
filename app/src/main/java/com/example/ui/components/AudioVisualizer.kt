package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AuraAccent
import com.example.ui.theme.LosslessGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class VisualizerType {
    WAVEFORM_BARS,
    RADIAL_SPECTRUM,
    LIQUID_WAVE
}

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    currentPositionMs: Long,
    modifier: Modifier = Modifier,
    visualizerType: VisualizerType = VisualizerType.WAVEFORM_BARS,
    barCount: Int = 36,
    accentColor: Color = AuraAccent,
    secondaryColor: Color = LosslessGold
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0 || height <= 0) return@Canvas

        when (visualizerType) {
            VisualizerType.WAVEFORM_BARS -> {
                val barWidth = (width / (barCount * 1.5f)).coerceAtLeast(3.dp.toPx())
                val spacing = barWidth * 0.5f
                val startX = (width - (barCount * (barWidth + spacing))) / 2f

                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = if (isPlaying) 0.95f else 0.4f),
                        secondaryColor.copy(alpha = if (isPlaying) 0.8f else 0.2f),
                        Color.Transparent
                    )
                )

                for (i in 0 until barCount) {
                    val x = startX + i * (barWidth + spacing)
                    val freq = (i + 1) * 0.35f
                    val baseFactor = if (isPlaying) {
                        (sin(phase + freq) * 0.4f + cos((phase * 1.5f) + i) * 0.3f + 0.5f).coerceIn(0.15f, 1.0f)
                    } else {
                        0.08f + (sin(i.toFloat()) * 0.04f)
                    }

                    val barHeight = (height * 0.65f * baseFactor).coerceAtLeast(6.dp.toPx())
                    val topY = (height - barHeight) / 2f

                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(x, topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }

            VisualizerType.RADIAL_SPECTRUM -> {
                val centerX = width / 2f
                val centerY = height / 2f
                val baseRadius = (width.coerceAtMost(height) * 0.28f) * (if (isPlaying) pulseScale else 1f)

                // Central pulsing aura glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (isPlaying) 0.35f else 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = baseRadius * 1.8f
                    ),
                    center = Offset(centerX, centerY),
                    radius = baseRadius * 1.8f
                )

                val radialBarCount = 48
                val angleStep = (2 * PI) / radialBarCount

                for (i in 0 until radialBarCount) {
                    val angle = i * angleStep + (if (isPlaying) phase * 0.2f else 0f)
                    val freq = i * 0.4f
                    val factor = if (isPlaying) {
                        (sin(phase + freq) * 0.45f + cos((phase * 1.2f) + i) * 0.35f + 0.5f).coerceIn(0.2f, 1.2f)
                    } else {
                        0.15f
                    }

                    val barLen = (height * 0.18f * factor)
                    val startX = centerX + cos(angle).toFloat() * baseRadius
                    val startY = centerY + sin(angle).toFloat() * baseRadius
                    val endX = centerX + cos(angle).toFloat() * (baseRadius + barLen)
                    val endY = centerY + sin(angle).toFloat() * (baseRadius + barLen)

                    val barColor = if (i % 2 == 0) accentColor else secondaryColor
                    drawLine(
                        color = barColor.copy(alpha = if (isPlaying) 0.85f else 0.3f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            VisualizerType.LIQUID_WAVE -> {
                val path = Path()
                val midY = height / 2f
                path.moveTo(0f, midY)

                val wavePoints = 50
                val stepX = width / wavePoints

                for (i in 0..wavePoints) {
                    val x = i * stepX
                    val angle = (i.toFloat() / wavePoints) * (4 * PI.toFloat()) + (if (isPlaying) phase * 2f else 0f)
                    val amplitude = if (isPlaying) (height * 0.2f) else (height * 0.03f)
                    val y = midY + sin(angle) * amplitude

                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(accentColor, secondaryColor, accentColor)
                    ),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}
