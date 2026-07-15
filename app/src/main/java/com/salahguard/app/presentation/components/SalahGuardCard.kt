package com.salahguard.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salahguard.app.presentation.designsystem.SGDimensions

/**
 * Quiet Supporting Card.
 *
 * This is deliberately the "quiet" sibling to the Hero countdown card:
 * flat by default (no elevation/shadow), a calmer corner radius, and a
 * softer border so supporting sections never compete with the Hero for
 * attention. Padding is sourced from the shared design-system scale
 * rather than a hardcoded value, and is overridable per-instance so
 * lightweight content (e.g. a single-line inline notice) isn't forced
 * into the same heavy padding as a full content card.
 */
@Composable
fun SalahGuardCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Float = 0f,
    showBorder: Boolean = false,
    contentPadding: Dp = SGDimensions.heroCardPadding,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        border = if (showBorder) {
            BorderStroke(
                width = 1.dp,
                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
            )
        } else null
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}