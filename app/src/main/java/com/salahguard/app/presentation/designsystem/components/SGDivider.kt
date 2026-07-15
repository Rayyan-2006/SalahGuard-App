package com.salahguard.app.presentation.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGDimensions
import com.salahguard.app.presentation.theme.SalahGuardTheme

/**
 * A hairline, low-contrast divider - never a heavy Material default line.
 * Used to separate sections within a card without breaking the calm,
 * glass aesthetic.
 */
@Composable
fun SGDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = SGDimensions.dividerThickness,
        color = SGColors.divider
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF040807)
@Composable
private fun SGDividerPreview() {
    SalahGuardTheme {
        SGDivider()
    }
}
