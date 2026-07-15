package com.salahguard.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahguard.app.presentation.theme.EmeraldGlowSoft
import com.salahguard.app.presentation.theme.GoldBright
import com.salahguard.app.presentation.theme.NightSurface
import com.salahguard.app.presentation.theme.SageMist
import com.salahguard.app.presentation.theme.WarmIvory

enum class BottomNavDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    PRAYERS("Prayers", Icons.Outlined.Schedule),
    LEARN("Learn", Icons.Outlined.MenuBook),
    JOURNEY("Journey", Icons.Outlined.Person),
    REFLECTION("Reflect", Icons.Outlined.Create)
}

/**
 * Premium glass-morphism bottom navigation.
 */
@Composable
fun SalahGuardBottomNavBar(
    selected: BottomNavDestination,
    onSelect: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .height(72.dp),
        color = NightSurface.copy(alpha = 0.82f),
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.08f)
        ),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavDestination.entries.forEach { destination ->
                val isSelected = selected == destination

                NavBarItem(
                    destination = destination,
                    isSelected = isSelected,
                    onClick = { onSelect(destination) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavBarItem(
    destination: BottomNavDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.45f,
        animationSpec = tween(400),
        label = "navItemAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "navItemScale"
    )

    val tint by animateColorAsState(
        targetValue = if (isSelected) GoldBright else WarmIvory,
        animationSpec = tween(400),
        label = "navItemColor"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = androidx.compose.ui.semantics.Role.Tab,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        // Selection Glow Logic
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .blur(24.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                EmeraldGlowSoft.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                tint = tint,
                modifier = Modifier.size(24.dp).alpha(alpha)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = destination.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) WarmIvory else SageMist,
                    letterSpacing = 0.4.sp
                ),
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}