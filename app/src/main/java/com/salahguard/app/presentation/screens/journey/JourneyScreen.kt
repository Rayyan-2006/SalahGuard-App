package com.salahguard.app.presentation.screens.journey

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salahguard.app.presentation.components.*
import com.salahguard.app.presentation.designsystem.*
import com.salahguard.app.presentation.designsystem.components.*
import com.salahguard.app.presentation.theme.*
import com.salahguard.app.presentation.screens.home.HomeViewModel
import com.salahguard.app.util.capitalizeFirst
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun JourneyScreen(
    viewModel: JourneyViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToPrayers: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToReflection: (String?) -> Unit,
    onNavigateToAchievements: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    HomeSanctuaryBackground(currentPrayerName = homeUiState.currentPrayerName) {
        var totalDrag by remember { mutableFloatStateOf(0f) }
        val haptic = LocalHapticFeedback.current
        val swipeScale by animateFloatAsState(
            targetValue = if (totalDrag != 0f) 0.98f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "swipeScale"
        )

        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = swipeScale
                    scaleY = swipeScale
                    translationX = totalDrag * 0.4f
                    alpha = (1f - (kotlin.math.abs(totalDrag) / 1000f)).coerceIn(0.7f, 1f)
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDrag += dragAmount
                        },
                        onDragEnd = {
                            if (totalDrag > 140) { // Swipe Right
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToQuran()
                            } else if (totalDrag < -140) { // Swipe Left
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToReflection(null)
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = { totalDrag = 0f }
                    )
                },
            bottomBar = {
                SalahGuardBottomNavBar(
                    selected = BottomNavDestination.JOURNEY,
                    onSelect = {
                        when (it) {
                            BottomNavDestination.HOME -> onNavigateToHome()
                            BottomNavDestination.PRAYERS -> onNavigateToPrayers()
                            BottomNavDestination.LEARN -> onNavigateToQuran()
                            BottomNavDestination.REFLECTION -> onNavigateToReflection(null)
                            else -> {}
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = SGSpacing.lg, vertical = SGSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(SGSpacing.xl)
            ) {
                // 1. Page Header
                item {
                    StaggeredEntrance(visible = visible, index = 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.xs)) {
                            Text(
                                text = "Journey",
                                style = SGTextStyles.heroTitle,
                                color = SGColors.textPrimary
                            )
                            Text(
                                text = "Every sincere prayer is a step closer.",
                                style = SGTextStyles.body.copy(
                                    color = SGColors.textSecondary.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SGColors.accent)
                        }
                    }
                } else {
                    // 2. Journey Summary Hero
                    item {
                        StaggeredEntrance(visible = visible, index = 1) {
                            JourneySummaryHero(uiState)
                        }
                    }

                    // 3. Prayer Heatmap
                    item {
                        StaggeredEntrance(visible = visible, index = 2) {
                            PrayerHeatmap(uiState.weeklyJourney, onNavigateToReflection)
                        }
                    }

                    // 4. Prayer Breakdown
                    item {
                        StaggeredEntrance(visible = visible, index = 3) {
                            PrayerBreakdown(uiState.prayerStats)
                        }
                    }

                    // 5. Milestones
                    item {
                        StaggeredEntrance(visible = visible, index = 4) {
                            JourneyMilestones(uiState)
                        }
                    }

                    // 6. Today's Insight
                    item {
                        val insight = uiState.insights.firstOrNull() ?: "Every prayer is a step closer to tranquility."
                        StaggeredEntrance(visible = visible, index = 5) {
                            TodayInsightCard(insight)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(SGSpacing.xl)) }
                }
            }
        }
    }
}

@Composable
private fun JourneySummaryHero(uiState: JourneyUiState) {
    SGHeroCard {
        SGStatRow(
            stats = listOf(
                SGStat(value = "${uiState.currentStreak}", label = "Current Streak"),
                SGStat(value = "${uiState.weeklyJourney.count { it.isFullyCompleted }}", label = "Days This Week"),
                SGStat(value = "${uiState.monthlyCompletionPercentage}%", label = "Monthly Progress")
            )
        )
    }
}

@Composable
private fun PrayerHeatmap(
    weeklyJourney: List<DayJourney>,
    onDayClick: (String?) -> Unit
) {
    SGGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
            SGSectionTitle(
                title = "Prayer History",
                subtitle = YearMonth.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            )
            
            // Simplified Heatmap - using weekly data to simulate a monthly view for this design task
            // In a real app, we'd pass the full month data.
            val today = LocalDate.now()
            val startOfMonth = today.withDayOfMonth(1)
            val firstDayOfWeek = startOfMonth.dayOfWeek.value % 7 // 0=Sunday
            val daysInMonth = YearMonth.now().lengthOfMonth()

            Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.sm)) {
                // Day labels
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            style = SGTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                            color = SGColors.textTertiary,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Grid
                var dayCount = 1
                for (week in 0..5) {
                    if (dayCount > daysInMonth) break
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        for (dayOfWeek in 0..6) {
                            if ((week == 0 && dayOfWeek < firstDayOfWeek) || dayCount > daysInMonth) {
                                Spacer(modifier = Modifier.size(32.dp))
                            } else {
                                val date = startOfMonth.plusDays((dayCount - 1).toLong())
                                val journeyDay = weeklyJourney.find { it.date == date }
                                
                                val intensity = when {
                                    journeyDay?.isFullyCompleted == true -> 1f
                                    (journeyDay?.completedCount ?: 0) > 2 -> 0.6f
                                    (journeyDay?.completedCount ?: 0) > 0 -> 0.3f
                                    date == today -> 0.1f // Today (empty but highlighted)
                                    else -> 0.05f
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (date == today) SGColors.accent.copy(alpha = 0.15f)
                                            else SGColors.accent.copy(alpha = intensity)
                                        )
                                        .border(
                                            width = if (date == today) 1.dp else 0.dp,
                                            color = if (date == today) SGColors.accentBright else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            // Format date as "MMM d" to match search in ReflectionScreen
                                            val query = date.format(DateTimeFormatter.ofPattern("MMM d"))
                                            onDayClick(query)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayCount.toString(),
                                        style = SGTextStyles.caption.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (intensity > 0.5f) NightBackground else SGColors.textPrimary
                                    )
                                }
                                dayCount++
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerBreakdown(stats: Map<String, PrayerAnalytics>) {
    SGGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
            SGSectionTitle(title = "Consistency Breakdown")
            
            val orderedPrayers = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
            Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.lg)) {
                orderedPrayers.forEach { name ->
                    val analytics = stats[name] ?: PrayerAnalytics(0, 0, 0)
                    Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.xs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name.capitalizeFirst(),
                                style = SGTextStyles.body.copy(fontWeight = FontWeight.Medium),
                                color = SGColors.textPrimary
                            )
                            Text(
                                text = "${analytics.completedCount} offered",
                                style = SGTextStyles.caption,
                                color = SGColors.textSecondary
                            )
                        }
                        SGProgressBar(
                            progress = analytics.percentage / 100f,
                            height = 6.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JourneyMilestones(uiState: JourneyUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
        SGSectionTitle(title = "Milestones")
        
        val milestones = listOf(
            Milestone("First Prayer", "Your journey began here.", uiState.totalCompletedPrayers > 0),
            Milestone("First Full Day", "A complete circle of devotion.", uiState.weeklyJourney.any { it.isFullyCompleted }),
            Milestone("Seven Day Consistency", "Building a sacred habit.", uiState.longestStreak >= 7),
            Milestone("Thirty Day Journey", "A month of spiritual growth.", uiState.longestStreak >= 30)
        )

        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
            milestones.forEachIndexed { index, milestone ->
                MilestoneItem(milestone)
                if (index < milestones.lastIndex) {
                    // Vertical line for timeline
                    Box(
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .width(2.dp)
                            .height(16.dp)
                            .background(SGColors.divider)
                    )
                }
            }
        }
    }
}

data class Milestone(val title: String, val subtitle: String, val isUnlocked: Boolean)

@Composable
private fun MilestoneItem(milestone: Milestone) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (milestone.isUnlocked) SGColors.accent.copy(alpha = 0.15f)
                    else Color.White.copy(alpha = 0.05f)
                )
                .border(
                    width = 1.dp,
                    color = if (milestone.isUnlocked) SGColors.accent else SGColors.glassBorderSubtle,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (milestone.isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                contentDescription = null,
                tint = if (milestone.isUnlocked) SGColors.accent else SGColors.textDisabled,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(SGSpacing.md))
        
        Column {
            Text(
                text = milestone.title,
                style = SGTextStyles.cardTitle.copy(fontSize = 16.sp),
                color = if (milestone.isUnlocked) SGColors.textPrimary else SGColors.textDisabled
            )
            Text(
                text = milestone.subtitle,
                style = SGTextStyles.caption,
                color = SGColors.textTertiary
            )
        }
    }
}

@Composable
private fun TodayInsightCard(insight: String) {
    SGGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SGColors.accent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SGColors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(SGSpacing.md))
                Text(
                    text = "TODAY'S INSIGHT",
                    style = SGTextStyles.label,
                    color = SGColors.accent
                )
            }
            
            Text(
                text = insight,
                style = SGTextStyles.body.copy(lineHeight = 24.sp, fontWeight = FontWeight.Medium),
                color = SGColors.textPrimary
            )
        }
    }
}

@Composable
private fun StaggeredEntrance(visible: Boolean, index: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { 24 },
            animationSpec = tween(durationMillis = 650, delayMillis = index * 70, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(650, delayMillis = index * 70, easing = FastOutSlowInEasing))
    ) {
        content()
    }
}
