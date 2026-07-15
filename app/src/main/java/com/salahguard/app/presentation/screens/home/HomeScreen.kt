package com.salahguard.app.presentation.screens.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salahguard.app.presentation.components.*
import com.salahguard.app.presentation.designsystem.SGSpacing
import com.salahguard.app.presentation.theme.*
import java.time.LocalDate

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPrayers: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToJourney: () -> Unit,
    onNavigateToReflection: (String?) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(BottomNavDestination.HOME) }
    var visible by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.loadData()
        }
    }

    LaunchedEffect(Unit) {
        visible = true
        val permissions = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    HomeSanctuaryBackground(currentPrayerName = uiState.currentPrayerName) {
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
                            if (totalDrag < -140) { // Swipe Left
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToPrayers()
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = { totalDrag = 0f }
                    )
                },
            bottomBar = {
                SalahGuardBottomNavBar(
                    selected = selectedTab,
                    onSelect = {
                        selectedTab = it
                        when (it) {
                            BottomNavDestination.PRAYERS -> onNavigateToPrayers()
                            BottomNavDestination.LEARN -> onNavigateToQuran()
                            BottomNavDestination.JOURNEY -> onNavigateToJourney()
                            BottomNavDestination.REFLECTION -> onNavigateToReflection(null)
                            else -> {}
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SGSpacing.lg, vertical = SGSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(SGSpacing.xl)
            ) {
                HomeHeader(
                    userName = uiState.userName,
                    onNotificationsClick = onNavigateToNotifications,
                    onSettingsClick = onNavigateToSettings
                )

                uiState.locationError?.let { error ->
                    SalahGuardCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        elevation = 0f,
                        contentPadding = SGSpacing.default
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(SGSpacing.default))
                            Text(
                                text = "Using estimated times. Enable location for precise moments.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                if (uiState.isLoading && uiState.remainingSeconds == 0L) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                if (uiState.showRecoveryCard) {
                    StaggeredEntrance(visible = visible, index = 0) {
                        HopeRecoveryCard(
                            message = uiState.recoveryMessage,
                            nextPrayer = uiState.nextPrayerName,
                            onAction = { viewModel.recoverLastMissedPrayer() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Hero Section: Prayer Countdown — isolated with extra breathing room
                // above and below (beyond the screen's base 24dp rhythm) so it reads
                // as the singular focal point rather than one item among equals.
                Spacer(modifier = Modifier.height(SGSpacing.default))
                StaggeredEntrance(visible = visible, index = 1) {
                    PrayerCountdownCard(
                        prayerName = uiState.nextPrayerName,
                        remainingSeconds = uiState.remainingSeconds,
                        currentPrayerName = uiState.currentPrayerName,
                        currentPrayerTime = uiState.currentPrayerTime,
                        nextPrayerTime = uiState.nextPrayerTime,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(SGSpacing.default))

                // Intention Card
                if (!uiState.isIntentionDismissed) {
                    val context = LocalContext.current
                    StaggeredEntrance(visible = visible, index = 2) {
                        DailyIntentionCard(
                            intention = uiState.dailyIntention?.text ?: "Focus on sincerity today.",
                            isCompleted = uiState.dailyIntention?.isCompleted ?: false,
                            isFavorite = uiState.dailyIntention?.isFavorite ?: false,
                            onTryClick = { viewModel.markIntentionCompleted() },
                            onDismissClick = { viewModel.dismissIntention() },
                            onFavoriteClick = { viewModel.toggleFavoriteIntention() },
                            onShareClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Today's Intention: ${uiState.dailyIntention?.text} #SalahGuard")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Intention"))
                            }
                        )
                    }
                }

                // Quran Section
                StaggeredEntrance(visible = visible, index = 3) {
                    QuranVerseCard(
                        arabicText = uiState.verseArabic,
                        translation = uiState.verseTranslation,
                        reference = uiState.verseReference,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Weekly progress
                StaggeredEntrance(visible = visible, index = 4) {
                    JourneyOverviewCard(uiState.weeklyProgress)
                }
            }
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

@Composable
private fun HomeHeader(userName: String, onNotificationsClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Assalamu Alaikum,",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PoppinsFontFamily, color = SageMist, letterSpacing = 0.5.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp, color = WarmIvory, fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "May Allah bless your day.",
                style = MaterialTheme.typography.labelMedium.copy(color = GoldBright.copy(alpha = 0.85f), letterSpacing = 0.4.sp, fontWeight = FontWeight.Medium)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassIconButton(icon = Icons.Outlined.Notifications, contentDescription = "Notifications", onClick = onNotificationsClick)
            GlassIconButton(icon = Icons.Outlined.Settings, contentDescription = "Settings", onClick = onSettingsClick)
        }
    }
}

@Composable
private fun GlassIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, onClick: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1f, animationSpec = tween(120), label = "iconButtonPressScale")

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        interactionSource = interactionSource,
        modifier = Modifier.size(48.dp).graphicsLayer { scaleX = pressScale; scaleY = pressScale }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = contentDescription, tint = WarmIvory.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun DailyIntentionCard(intention: String, isCompleted: Boolean, isFavorite: Boolean, onTryClick: () -> Unit, onDismissClick: () -> Unit, onFavoriteClick: () -> Unit, onShareClick: () -> Unit) {
    SalahGuardCard(modifier = Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(4.dp).height(48.dp).clip(CircleShape).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))))
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "🌱 DAILY INTENTION", style = MaterialTheme.typography.labelSmall, color = SageMist, letterSpacing = 1.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = intention, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium, color = WarmIvory))
                }
                if (isCompleted) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Completed", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row {
                    IconButton(onClick = onFavoriteClick) { Icon(imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = if (isFavorite) MaterialTheme.colorScheme.primary else SageMist, modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = onShareClick) { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Share, contentDescription = "Share", tint = SageMist, modifier = Modifier.size(20.dp)) }
                }
                if (!isCompleted) {
                    Row {
                        TextButton(onClick = onDismissClick) { Text("Dismiss", color = SageMist) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onTryClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.primary)) { Text("I'll Practice This Today") }
                    }
                }
            }
        }
    }
}

@Composable
private fun JourneyOverviewCard(weeklyProgress: List<Boolean>) {
    val completionCount = weeklyProgress.count { it }
    val targetRate = if (weeklyProgress.isNotEmpty()) (completionCount.toFloat() / weeklyProgress.size.toFloat()) else 0f
    val completionRate by animateFloatAsState(targetValue = targetRate, animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing), label = "completionRate")

    SalahGuardCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "CONSISTENCY", style = MaterialTheme.typography.labelSmall, color = SageMist, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Your Journey", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium, color = WarmIvory))
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { completionRate }, modifier = Modifier.size(56.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), strokeWidth = 6.dp)
                Text(text = "${(completionRate * 100).toInt()}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = WarmIvory)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val days = listOf("S", "M", "T", "W", "T", "F", "S")
            repeat(7) { i ->
                val isCompleted = weeklyProgress.getOrElse(i) { false }
                val date = LocalDate.now().minusDays(6 - i.toLong())
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = days[(date.dayOfWeek.value % 7)], style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal, fontSize = 10.sp), color = if (isCompleted) WarmIvory else SageMist.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f)), contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        } else {
                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SageMist.copy(alpha = 0.25f)))
                        }
                    }
                }
            }
        }
    }
}