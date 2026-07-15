package com.salahguard.app.presentation.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGDimensions
import com.salahguard.app.presentation.theme.SalahGuardTheme

/**
 * Animated, rounded-end progress bar. Used for prayer-streak / weekly
 * progress today, reusable anywhere a linear progress indicator is needed
 * (Journey screen, achievements, etc).
 *
 * @param progress 0f..1f. Values outside that range are clamped.
 * @param progressBrush defaults to the design system's gold-leaf gradient;
 * pass a solid Brush.linearGradient(listOf(color, color)) for a flat fill.
 */
@Composable
fun SGProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = SGDimensions.progressBarHeight,
    trackColor: Color = SGColors.progressTrack,
    progressBrush: Brush = Brush.linearGradient(SGColors.progressFillGradient)
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped,
        animationSpec = tween(durationMillis = 400),
        label = "sg_progress_bar"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(percent = 50))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = animatedProgress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(percent = 50))
                .background(progressBrush)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF040807)
@Composable
private fun SGProgressBarPreview() {
    SalahGuardTheme {
        SGProgressBar(progress = 0.65f)
    }
}
