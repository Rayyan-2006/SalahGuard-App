package com.salahguard.app.presentation.screens.prayers

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahguard.app.domain.model.Prayer
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.domain.model.PrayerStatus
import com.salahguard.app.presentation.designsystem.*
import com.salahguard.app.presentation.designsystem.components.SGDivider
import com.salahguard.app.presentation.theme.GoldBright
import com.salahguard.app.presentation.theme.SuccessGreen
import com.salahguard.app.presentation.theme.ForestGreen
import java.time.format.DateTimeFormatter

@Composable
fun TodayJourneyCard(
    currentPrayer: Prayer?,
    nextPrayer: Prayer?,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow),
        label = "journeyProgress"
    )

    // Exact 28dp radius as requested
    val cardShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = cardShape)
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E4D3E).copy(alpha = 0.4f), // Lighter, more translucent emerald
                        Color(0xFF122421).copy(alpha = 0.3f)  // Soft glass base
                    )
                ),
                shape = cardShape
            )
            .border(1.dp, Color.White.copy(alpha = 0.15f), cardShape)
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Prayer",
                        style = SGTextStyles.label,
                        color = SGColors.textSecondary
                    )
                    Text(
                        text = currentPrayer?.name?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "—",
                        style = SGTextStyles.sectionTitle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                        color = SGColors.accentBright
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Next",
                        style = SGTextStyles.label,
                        color = SGColors.textSecondary
                    )
                    Text(
                        text = nextPrayer?.name?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Fajr",
                        style = SGTextStyles.sectionTitle.copy(fontSize = 22.sp),
                        color = SGColors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Progress",
                        style = SGTextStyles.label,
                        color = SGColors.textSecondary
                    )
                    Text(
                        text = "$completedCount / $totalCount",
                        style = SGTextStyles.label.copy(fontWeight = FontWeight.Bold, color = SGColors.accentBright),
                    )
                }
                Spacer(modifier = Modifier.height(SGSpacing.sm))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = SGColors.accentBright,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )
            }
        }
    }
}

@Composable
fun PrayerTimelineItem(
    prayer: Prayer,
    isCurrent: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val isDone = prayer.status == PrayerStatus.COMPLETED || prayer.status == PrayerStatus.RECOVERED
    val isMissed = prayer.status == PrayerStatus.MISSED
    val isSunrise = prayer.name == PrayerName.SUNRISE

    val accentColor by animateColorAsState(
        targetValue = when {
            isDone -> SGColors.success.copy(alpha = 0.9f)
            isCurrent -> SGColors.accentBright
            isMissed -> SGColors.textTertiary.copy(alpha = 0.7f)
            isSunrise -> SGColors.textTertiary.copy(alpha = 0.5f)
            else -> SGColors.textSecondary.copy(alpha = 0.6f)
        },
        animationSpec = tween(500), label = "timelineAccent"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCurrent -> SGColors.accentBright.copy(alpha = 0.06f)
            isDone -> SGColors.success.copy(alpha = 0.03f)
            else -> Color.Transparent
        },
        animationSpec = tween(500), label = "timelineBackground"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SGShapes.medium)
            .background(backgroundColor)
            .then(
                if (isCurrent) Modifier.border(1.dp, SGColors.accentBright.copy(alpha = 0.1f), SGShapes.medium)
                else Modifier
            )
            .clickable(enabled = !isSunrise) { onClick() }
            .padding(vertical = SGSpacing.md, horizontal = SGSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = isDone, label = "timelineIcon") { done ->
                if (done) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(SGSpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prayer.name.name.lowercase().replaceFirstChar { it.uppercase() },
                style = SGTextStyles.cardTitle.copy(
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSunrise) SGColors.textTertiary else SGColors.textPrimary
                )
            )
            Text(
                text = prayer.scheduledTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                style = SGTextStyles.caption.copy(letterSpacing = 0.5.sp),
                color = SGColors.textSecondary
            )
        }

        AnimatedVisibility(
            visible = isCurrent || isMissed || isDone,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + itemFadeOut()
        ) {
            val statusText = when {
                isCurrent -> "Current"
                isMissed -> "Missed"
                prayer.status == PrayerStatus.RECOVERED -> "Recovered"
                else -> "Completed"
            }
            val statusColor = when {
                isCurrent -> SGColors.accentBright
                isMissed -> SGColors.textTertiary
                else -> SGColors.success
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.08f),
                shape = SGShapes.pill,
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.15f)),
                modifier = Modifier.padding(start = SGSpacing.sm)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = SGTextStyles.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }
        }
    }
}

private fun itemFadeOut() = androidx.compose.animation.fadeOut(animationSpec = tween(300))

@Composable
fun DailyProgressCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.then(SGGlass.subtle()),
        shape = SGShapes.medium,
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(SGSpacing.default),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(SGSpacing.xs))
            Text(
                text = value,
                style = SGTextStyles.sectionTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                color = SGColors.textPrimary
            )
            Text(
                text = label,
                style = SGTextStyles.label.copy(fontSize = 10.sp),
                color = SGColors.textSecondary
            )
        }
    }
}
