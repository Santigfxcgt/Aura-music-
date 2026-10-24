package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuraAccent
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.NavigationTab

@Composable
fun FloatingBottomBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassBox(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            backgroundColor = Color(0xEB13141B),
            borderColor = GlassCardBorder,
            borderWidth = 1.dp,
            elevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "Inicio",
                    selected = selectedTab == NavigationTab.HOME,
                    activeIcon = Icons.Filled.Home,
                    inactiveIcon = Icons.Outlined.Home,
                    testTag = "nav_tab_home",
                    onClick = { onTabSelected(NavigationTab.HOME) }
                )

                BottomNavItem(
                    label = "Canciones",
                    selected = selectedTab == NavigationTab.SONGS,
                    activeIcon = Icons.Filled.MusicNote,
                    inactiveIcon = Icons.Outlined.MusicNote,
                    testTag = "nav_tab_songs",
                    onClick = { onTabSelected(NavigationTab.SONGS) }
                )

                BottomNavItem(
                    label = "Biblioteca",
                    selected = selectedTab == NavigationTab.LIBRARY,
                    activeIcon = Icons.Filled.LibraryMusic,
                    inactiveIcon = Icons.Outlined.LibraryMusic,
                    testTag = "nav_tab_library",
                    onClick = { onTabSelected(NavigationTab.LIBRARY) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else TextMuted,
        animationSpec = spring(),
        label = "iconColor"
    )

    val labelColor by animateColorAsState(
        targetValue = if (selected) AuraAccent else TextMuted,
        animationSpec = spring(),
        label = "labelColor"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .then(
                        if (selected) {
                            Modifier
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0x33FFFFFF),
                                            Color.Transparent
                                        )
                                    )
                                )
                        } else Modifier
                    )
            ) {
                Icon(
                    imageVector = if (selected) activeIcon else inactiveIcon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = label,
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.2.sp
            )
        }
    }
}
