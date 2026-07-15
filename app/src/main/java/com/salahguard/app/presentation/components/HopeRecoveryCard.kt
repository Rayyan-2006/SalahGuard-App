package com.salahguard.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahguard.app.presentation.theme.EmeraldGlowSoft
import com.salahguard.app.presentation.theme.GoldBright
import com.salahguard.app.presentation.theme.NightBackground
import com.salahguard.app.presentation.theme.SageMist
import com.salahguard.app.presentation.theme.WarmIvory

/**
 * "Hope Over Guilt" recovery card — unchanged philosophy and content
 * contract (message, nextPrayer, onAction are exactly as before).
 *
 * Sprint 11A redesign: the previous version leaned on bright gold at low
 * opacity, which read closer to a warning banner than a comforting note.
 * This version uses a soft emerald glass tone instead — gold is kept only
 * as a small, deliberate accent (the heart icon and the CTA label) — so the
 * card feels like a gentle hand on the shoulder rather than an alert, and
 * doesn't out-compete the hero countdown card for attention below it.
 */
@Composable
fun HopeRecoveryCard(
    message: String,
    nextPrayer: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    SalahGuardCard(
        modifier = modifier,
        containerColor = EmeraldGlowSoft.copy(alpha = 0.10f),
        elevation = 0f,
        showBorder = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EmeraldGlowSoft.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "A Moment of Grace",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = WarmIvory,
                        letterSpacing = 0.3.sp
                    )
                )
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp,
                    color = WarmIvory.copy(alpha = 0.88f)
                )
            )

            HorizontalDivider(
                color = WarmIvory.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEXT OPPORTUNITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageMist,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = nextPrayer,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = WarmIvory
                        )
                    )
                }

                Button(
                    onClick = onAction,
                    modifier = Modifier
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp)),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = NightBackground
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(EmeraldGlowSoft.copy(alpha = 0.45f))
                            .padding(horizontal = 22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Prepare",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = GoldBright,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}