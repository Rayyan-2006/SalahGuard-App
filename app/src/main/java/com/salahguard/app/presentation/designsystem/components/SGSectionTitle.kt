package com.salahguard.app.presentation.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGSpacing
import com.salahguard.app.presentation.designsystem.SGTextStyles
import com.salahguard.app.presentation.theme.SalahGuardTheme

/**
 * Standard section header - title, optional subtitle beneath it, and an
 * optional trailing text action (e.g. "See all"). Reusable across every
 * screen so section headers stop being ad-hoc Text() pairs.
 */
@Composable
fun SGSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = SGTextStyles.sectionTitle, color = SGColors.textPrimary)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = SGTextStyles.caption,
                    color = SGColors.textTertiary,
                    modifier = Modifier.padding(top = SGSpacing.xs)
                )
            }
        }

        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionLabel,
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = SGColors.accent
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF040807)
@Composable
private fun SGSectionTitlePreview() {
    SalahGuardTheme {
        SGSectionTitle(
            title = "This Week",
            subtitle = "Your prayer journey",
            actionLabel = "See all",
            onActionClick = {}
        )
    }
}
