package com.salahguard.app.presentation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGDimensions
import com.salahguard.app.presentation.designsystem.SGShapes
import com.salahguard.app.presentation.designsystem.SGSpacing
import com.salahguard.app.presentation.theme.SalahGuardTheme

/**
 * Low-emphasis outlined action button - secondary CTAs, "skip" / "not now"
 * type actions that shouldn't compete visually with an SGPrimaryButton.
 */
@Composable
fun SGSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(SGDimensions.buttonHeight),
        enabled = enabled,
        shape = SGShapes.large,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SGColors.accent,
            disabledContentColor = SGColors.textDisabled
        ),
        border = BorderStroke(
            width = SGDimensions.borderWidthStandard,
            color = if (enabled) SGColors.accent.copy(alpha = 0.5f) else SGColors.textDisabled.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(horizontal = SGSpacing.xl)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.25.sp,
                fontSize = 14.sp,
                color = if (enabled) SGColors.accent else SGColors.textDisabled
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF040807)
@Composable
private fun SGSecondaryButtonPreview() {
    SalahGuardTheme {
        SGSecondaryButton(text = "Not now", onClick = {})
    }
}
