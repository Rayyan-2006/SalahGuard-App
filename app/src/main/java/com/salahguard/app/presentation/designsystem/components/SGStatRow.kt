package com.salahguard.app.presentation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGSpacing
import com.salahguard.app.presentation.designsystem.SGTextStyles
import com.salahguard.app.presentation.theme.SalahGuardTheme

/**
 * One statistic in a row: a large number/value with a small caption
 * beneath it (e.g. "12" / "Day Streak"). [SGStatRow] lays out one or more
 * of these evenly across the available width - used for weekly summaries,
 * achievement counts, and similar at-a-glance numbers.
 */
data class SGStat(
    val value: String,
    val label: String
)

@Composable
fun SGStatRow(
    stats: List<SGStat>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        stats.forEach { stat ->
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = stat.value,
                    style = SGTextStyles.statNumber,
                    color = SGColors.textPrimary
                )
                Text(
                    text = stat.label,
                    style = SGTextStyles.caption,
                    color = SGColors.textTertiary,
                    modifier = Modifier.padding(top = SGSpacing.xs)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF040807)
@Composable
private fun SGStatRowPreview() {
    SalahGuardTheme {
        SGStatRow(
            stats = listOf(
                SGStat(value = "12", label = "Day Streak"),
                SGStat(value = "87%", label = "This Week"),
                SGStat(value = "5", label = "Prayers Today")
            )
        )
    }
}
