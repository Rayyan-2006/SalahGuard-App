package com.salahguard.app.presentation.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGDimensions
import com.salahguard.app.presentation.designsystem.SGElevation
import com.salahguard.app.presentation.designsystem.SGGlass
import com.salahguard.app.presentation.designsystem.SGShapes
import com.salahguard.app.presentation.theme.SalahGuardTheme

/**
 * The standard reusable card - Quran, Daily Intention, Reflection, stat
 * blocks, etc. Medium elevation, medium glass. This is the workhorse card
 * of the design system; most screen content should live inside one of
 * these rather than a one-off Card.
 *
 * @param onClick optional; when provided the card becomes tappable with a
 * ripple, when null it renders as static content.
 */
@Composable
fun SGGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(SGGlass.standard())
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = SGShapes.large,
        color = Color.Transparent,
        shadowElevation = SGElevation.raised
    ) {
        Column(
            modifier = Modifier.padding(SGDimensions.glassCardPadding)
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF040807)
@Composable
private fun SGGlassCardPreview() {
    SalahGuardTheme {
        SGGlassCard {
            Text(
                text = "Glass card content",
                color = SGColors.textPrimary
            )
        }
    }
}
