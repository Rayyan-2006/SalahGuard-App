package com.salahguard.app.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGDimensions
import com.salahguard.app.presentation.designsystem.SGShapes
import com.salahguard.app.presentation.designsystem.SGTextStyles
import com.salahguard.app.presentation.theme.SalahGuardTheme

/**
 * Status/tag chip. Style drives both container and content color from
 * design tokens - callers never pass a raw Color.
 */
enum class SGChipStyle {
    Active,
    Inactive,
    Success,
    Warning
}

@Composable
fun SGChip(
    text: String,
    modifier: Modifier = Modifier,
    style: SGChipStyle = SGChipStyle.Inactive
) {
    val (containerColor, contentColor) = style.colors()

    Box(
        modifier = modifier
            .height(SGDimensions.chipHeight)
            .clip(SGShapes.pill)
            .background(containerColor)
            .padding(horizontal = SGDimensions.chipHorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SGTextStyles.label,
            color = contentColor
        )
    }
}

private fun SGChipStyle.colors(): Pair<Color, Color> = when (this) {
    SGChipStyle.Active -> SGColors.chipActiveContainer to SGColors.chipActiveContent
    SGChipStyle.Inactive -> SGColors.chipInactiveContainer to SGColors.chipInactiveContent
    SGChipStyle.Success -> SGColors.chipSuccessContainer to SGColors.chipSuccessContent
    SGChipStyle.Warning -> SGColors.chipWarningContainer to SGColors.chipWarningContent
}

@Preview(showBackground = true, backgroundColor = 0xFF040807)
@Composable
private fun SGChipPreview() {
    SalahGuardTheme {
        SGChip(text = "Completed", style = SGChipStyle.Success)
    }
}
