package com.salahguard.app.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGDimensions
import com.salahguard.app.presentation.designsystem.SGShapes
import com.salahguard.app.presentation.designsystem.SGSpacing
import com.salahguard.app.presentation.theme.NightBackground
import com.salahguard.app.presentation.theme.SalahGuardTheme

/**
 * The filled, high-emphasis action button. Gold-leaf gradient fill,
 * generous tap target, uppercase label - the same visual language as the
 * app's existing primary CTA, now sourced entirely from design tokens.
 */
@Composable
fun SGPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(SGDimensions.buttonHeight),
        enabled = enabled,
        shape = SGShapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = NightBackground,
            disabledContainerColor = SGColors.textPrimary.copy(alpha = 0.12f),
            disabledContentColor = SGColors.textPrimary.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) {
                        Brush.linearGradient(SGColors.accentGradient)
                    } else {
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .padding(horizontal = SGSpacing.xl),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 14.sp,
                    color = if (enabled) NightBackground else SGColors.textPrimary.copy(alpha = 0.38f)
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF040807)
@Composable
private fun SGPrimaryButtonPreview() {
    SalahGuardTheme {
        SGPrimaryButton(text = "Continue", onClick = {})
    }
}
