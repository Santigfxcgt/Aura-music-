package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassCardBackgroundDark
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderSubtle

/**
 * Apple Music-inspired Liquid Glass Container.
 * Features a frosted translucent surface, specular rim reflection gradient (top-lit),
 * and subtle ambient depth shadow.
 */
@Composable
fun GlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = GlassCardBackgroundDark,
    borderColor: Color = GlassCardBorder,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0x66000000),
                spotColor = Color(0x88000000)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                BorderStroke(
                    borderWidth,
                    Brush.verticalGradient(
                        colors = listOf(
                            borderColor,
                            GlassCardBorderSubtle
                        )
                    )
                ),
                shape = shape
            ),
        content = content
    )
}
