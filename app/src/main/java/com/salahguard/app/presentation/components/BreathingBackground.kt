package com.salahguard.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.salahguard.app.presentation.theme.NightBackground
import com.salahguard.app.presentation.theme.SanctuaryGradient

/**
 * Enhanced sanctuary background with subtle Islamic geometry and depth.
 */
@Composable
fun BreathingBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "sanctuary")
    
    val pulse by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Base dark sanctuary gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(SanctuaryGradient))
        )

        // Subtle atmospheric glow
        Canvas(modifier = Modifier.fillMaxSize().alpha(pulse)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2E7D62).copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.2f)
                )
            )
        }

        // Overlay content
        content()
    }
}
