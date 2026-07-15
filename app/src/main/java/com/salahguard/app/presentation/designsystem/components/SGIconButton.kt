package com.salahguard.app.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGDimensions
import com.salahguard.app.presentation.designsystem.SGShapes
import com.salahguard.app.presentation.theme.SalahGuardTheme

/**
 * Circular icon-only button - back/close affordances, card-corner actions,
 * bottom-nav-adjacent controls. Keeps the ripple and accessible tap target
 * of a standard IconButton, wrapped in the design system's subtle-glass
 * circular background.
 */
@Composable
fun SGIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = SGColors.textPrimary,
    containerColor: Color = SGColors.glassFillSubtle,
    size: Dp = SGDimensions.iconButtonSize
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .background(color = containerColor, shape = SGShapes.pill)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(SGDimensions.iconSize)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF040807)
@Composable
private fun SGIconButtonPreview() {
    SalahGuardTheme {
        SGIconButton(
            icon = Icons.Filled.Favorite,
            contentDescription = "Favorite",
            onClick = {}
        )
    }
}
