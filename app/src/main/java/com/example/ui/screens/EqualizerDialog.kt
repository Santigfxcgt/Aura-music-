package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassBox
import com.example.ui.theme.AuraAccent
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.LosslessGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

val EQ_FREQUENCIES = listOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")

val EQ_PRESETS_MAP = mapOf(
    "Plano" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    "Bajos Boost" to listOf(8f, 7f, 5f, 3f, 1f, 0f, -1f, -2f, -1f, 0f),
    "EDM / Electrónica" to listOf(6f, 5f, 3f, 0f, -2f, 2f, 4f, 5f, 6f, 7f),
    "Rock" to listOf(5f, 4f, 2f, -1f, -2f, 1f, 3f, 4f, 5f, 5f),
    "Pop" to listOf(-1f, 1f, 3f, 4f, 4f, 3f, 1f, -1f, -2f, -2f),
    "Voz Cristal" to listOf(-3f, -2f, -1f, 1f, 4f, 5f, 4f, 2f, 1f, 0f),
    "Acústico" to listOf(3f, 2f, 1f, 1f, 2f, 3f, 3f, 2f, 2f, 1f),
    "Club / Fiesta" to listOf(4f, 4f, 3f, 2f, 0f, 0f, 2f, 3f, 4f, 4f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerDialog(
    isEnabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    eqBands: List<Float>,
    onBandChange: (bandIndex: Int, gainDb: Float) -> Unit,
    bassBoostPercent: Int,
    onBassBoostChange: (Int) -> Unit,
    virtualizerPercent: Int,
    onVirtualizerChange: (Int) -> Unit,
    reverbPreset: String,
    onReverbPresetChange: (String) -> Unit,
    selectedPresetName: String,
    onSelectPreset: (String) -> Unit,
    onResetEq: () -> Unit,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag("equalizer_dialog")
    ) {
        GlassBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(28.dp),
            backgroundColor = GlassSurfaceElevated,
            borderColor = GlassCardBorder,
            borderWidth = 1.dp,
            elevation = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header: Title & Master Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x338B5CF6))
                                .border(0.5.dp, GlassCardBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Equalizer,
                                contentDescription = null,
                                tint = LosslessGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ecualizador Pro 10-Bandas",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEnabled) "Procesamiento DSP Activo" else "Bypass / Desactivado",
                                color = if (isEnabled) LosslessGold else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = onToggleEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AuraAccent,
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("eq_master_switch")
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets Ribbon Selector
                Text(
                    text = "Presets de Ecualización",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EQ_PRESETS_MAP.keys.forEach { preset ->
                        val isSelected = preset == selectedPresetName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) AuraAccent else Color(0x1FFFFFFF))
                                .border(0.5.dp, if (isSelected) AuraAccent else GlassCardBorder, RoundedCornerShape(12.dp))
                                .clickable { onSelectPreset(preset) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = preset,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency Curve Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x2B10111A))
                        .border(0.5.dp, GlassCardBorder, RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height
                        val midY = h / 2f

                        // Draw zero line
                        drawLine(
                            color = Color(0x22FFFFFF),
                            start = Offset(0f, midY),
                            end = Offset(w, midY),
                            strokeWidth = 1.dp.toPx()
                        )

                        if (eqBands.isNotEmpty()) {
                            val stepX = w / (eqBands.size - 1)
                            val path = Path()

                            eqBands.forEachIndexed { i, gainDb ->
                                val x = i * stepX
                                // gainDb ranges from -12dB to +12dB
                                val normalizedGain = (gainDb / 12f).coerceIn(-1f, 1f)
                                val y = midY - (normalizedGain * (h * 0.4f))

                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

                                // Draw node point
                                drawCircle(
                                    color = if (isEnabled) LosslessGold else TextMuted,
                                    radius = 3.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }

                            drawPath(
                                path = path,
                                brush = Brush.horizontalGradient(
                                    colors = if (isEnabled) listOf(AuraAccent, LosslessGold, AuraAccent) else listOf(TextMuted, TextMuted)
                                ),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 10-Band Vertical Sliders Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EQ_FREQUENCIES.forEachIndexed { index, freq ->
                        val currentGain = eqBands.getOrElse(index) { 0f }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(44.dp)
                                .fillMaxHeight()
                        ) {
                            Text(
                                text = "${currentGain.toInt()}dB",
                                color = if (currentGain != 0f) LosslessGold else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Vertical Slider
                            Slider(
                                value = currentGain,
                                onValueChange = { onBandChange(index, it) },
                                valueRange = -12f..12f,
                                enabled = isEnabled,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("eq_slider_$index"),
                                colors = SliderDefaults.colors(
                                    thumbColor = if (isEnabled) AuraAccent else TextMuted,
                                    activeTrackColor = if (isEnabled) LosslessGold else TextMuted,
                                    inactiveTrackColor = Color(0x22FFFFFF)
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = freq,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sound Processing Controls: Bass Boost, Virtualizer 3D, Reverb
                Text(
                    text = "Efectos DSP & Procesamiento Acústico",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bass Boost Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = AuraAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bajos Profundos (Bass Boost)",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "$bassBoostPercent%",
                            color = LosslessGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = bassBoostPercent.toFloat(),
                        onValueChange = { onBassBoostChange(it.toInt()) },
                        valueRange = 0f..100f,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = AuraAccent,
                            activeTrackColor = AuraAccent,
                            inactiveTrackColor = Color(0x22FFFFFF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3D Spatial Virtualizer Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.SurroundSound,
                                contentDescription = null,
                                tint = AuraAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Virtualizador 3D Espacial",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "$virtualizerPercent%",
                            color = LosslessGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = virtualizerPercent.toFloat(),
                        onValueChange = { onVirtualizerChange(it.toInt()) },
                        valueRange = 0f..100f,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = AuraAccent,
                            activeTrackColor = AuraAccent,
                            inactiveTrackColor = Color(0x22FFFFFF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reverb Presets
                Text(
                    text = "Reverberación de Sala",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                val reverbs = listOf(
                    "OFF" to "Apagado",
                    "SMALL_ROOM" to "Sala",
                    "MEDIUM_HALL" to "Salón",
                    "LARGE_HALL" to "Gran Hall",
                    "PLATE" to "Estadio"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    reverbs.forEach { (key, label) ->
                        val isSelected = key == reverbPreset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AuraAccent else Color(0x1AFFFFFF))
                                .clickable(enabled = isEnabled) { onReverbPresetChange(key) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Dialog Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetEq,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassCardBorder)
                    ) {
                        Icon(imageVector = Icons.Filled.RestartAlt, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resetear", color = TextMuted, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuraAccent)
                    ) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aplicar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
