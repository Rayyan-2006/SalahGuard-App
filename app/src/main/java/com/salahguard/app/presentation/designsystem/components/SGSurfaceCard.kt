package com.salahguard.app.presentation.designsystem.components

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
 * A very subtle, almost-transparent container - used to group content
 * *inside* an SGHeroCard or SGGlassCard (e.g. a stat block within a
 * bigger card), not as a top-level screen card.
 */
@Composable
fun SGSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(SGGlass.subtle()),
        shape = SGShapes.medium,
        color = Color.Transparent,
        shadowElevation = SGElevation.flat
    ) {
        Column(
            modifier = Modifier.padding(SGDimensions.surfaceCardPadding)
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF040807)
@Composable
private fun SGSurfaceCardPreview() {
    SalahGuardTheme {
        SGSurfaceCard {
            Text(
                text = "Surface card content",
                color = SGColors.textPrimary
            )
        }
    }
}
