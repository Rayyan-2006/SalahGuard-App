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
import androidx.compose.ui.unit.Dp
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGDimensions
import com.salahguard.app.presentation.designsystem.SGElevation
import com.salahguard.app.presentation.designsystem.SGGlass
import com.salahguard.app.presentation.designsystem.SGShapes
import com.salahguard.app.presentation.theme.SalahGuardTheme

/**
 * The largest, most prominent card on a screen - used only once per screen
 * (e.g. the current-prayer countdown on Home). Premium glass fill, the
 * strongest elevation in the system, and the most generous padding.
 *
 * @param modifier applied to the outer surface.
 * @param elevation defaults to [SGElevation.hero]; override only for rare
 * cases (e.g. a pressed state), never to introduce a new hardcoded value.
 * @param content the card body. Receives no padding modifier itself -
 * [SGDimensions.heroCardPadding] is already applied around it.
 */
@Composable
fun SGHeroCard(
    modifier: Modifier = Modifier,
    elevation: Dp = SGElevation.hero,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(SGGlass.hero()),
        shape = SGShapes.hero,
        color = Color.Transparent,
        shadowElevation = elevation
    ) {
        Column(
            modifier = Modifier.padding(SGDimensions.heroCardPadding)
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF040807)
@Composable
private fun SGHeroCardPreview() {
    SalahGuardTheme {
        SGHeroCard {
            Text(
                text = "Hero card content",
                color = SGColors.textPrimary
            )
        }
    }
}
