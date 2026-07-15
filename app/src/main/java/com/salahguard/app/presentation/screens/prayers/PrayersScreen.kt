package com.salahguard.app.presentation.screens.prayers

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salahguard.app.R
import com.salahguard.app.domain.model.Prayer
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.domain.model.PrayerStatus
import com.salahguard.app.presentation.components.BottomNavDestination
import com.salahguard.app.presentation.components.HomeSanctuaryBackground
import com.salahguard.app.presentation.components.SalahGuardBottomNavBar
import com.salahguard.app.presentation.components.SalahGuardCard
import com.salahguard.app.presentation.designsystem.*
import com.salahguard.app.presentation.designsystem.components.*
import com.salahguard.app.presentation.theme.GoldBright
import com.salahguard.app.presentation.theme.MutedSand
import com.salahguard.app.presentation.theme.SuccessGreen
import com.salahguard.app.util.capitalizeFirst
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayersScreen(
    viewModel: PrayersViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToJourney: () -> Unit,
    onNavigateToReflection: (String?) -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToMosques: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(BottomNavDestination.PRAYERS) }
    val haptic = LocalHapticFeedback.current
    var visible by remember { mutableStateOf(false) }
    var detailPrayer by remember { mutableStateOf<Prayer?>(null) }
    
    var showToolsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.loadData()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    HomeSanctuaryBackground(currentPrayerName = currentPrayerOf(uiState.prayers)?.name?.name) {
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
                                onNavigateToHome()
                            } else if (totalDrag < -140) { // Swipe Left
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToQuran()
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = { totalDrag = 0f }
                    )
                },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Prayer",
                            style = SGTextStyles.sectionTitle,
                            color = SGColors.textPrimary
                        )
                    },
                    actions = {
                        Box(contentAlignment = Alignment.Center) {
                            // Subtle rotating glow behind the compass
                            val rotation by animateFloatAsState(
                                targetValue = -uiState.azimuth,
                                animationSpec = tween(500),
                                label = "TopCompassRotation"
                            )

                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .rotate(rotation),
                                shape = CircleShape,
                                color = Color.Transparent,
                                border = BorderStroke(
                                    width = 1.dp,
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            SGColors.accentBright.copy(alpha = 0.1f),
                                            SGColors.accentBright.copy(alpha = 0.6f),
                                            SGColors.accentBright.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                            ) {
                                IconButton(onClick = { showToolsSheet = true }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_qibla_compass),
                                        contentDescription = "Prayer Tools",
                                        tint = SGColors.textPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            // Fixed North Indicator dot
                            Box(
                                modifier = Modifier
                                    .size(40.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .padding(top = 2.dp)
                                        .clip(CircleShape)
                                        .background(SGColors.accentBright)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                SalahGuardBottomNavBar(
                    selected = selectedTab,
                    onSelect = {
                        selectedTab = it
                        when (it) {
                            BottomNavDestination.HOME -> onNavigateToHome()
                            BottomNavDestination.LEARN -> onNavigateToQuran()
                            BottomNavDestination.JOURNEY -> onNavigateToJourney()
                            BottomNavDestination.REFLECTION -> onNavigateToReflection(null)
                            else -> {}
                        }
                    }
                )
            }
        ) { padding ->
            if (uiState.isLoading && uiState.prayers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = SGSpacing.lg, vertical = SGSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(SGSpacing.xl)
                ) {
                    // 1. Large Page Title & Date
                    StaggeredEntrance(visible = visible, index = 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.xs)) {
                            Text(
                                text = "Today's Prayers",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                text = uiState.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    uiState.locationError?.let { error ->
                        LocationNotice(error)
                    }

                    // 2. Today's Journey Card
                    val prayables = uiState.prayers.filter { it.name != PrayerName.SUNRISE }
                    val completed = prayables.count { it.status == PrayerStatus.COMPLETED || it.status == PrayerStatus.RECOVERED }
                    val total = prayables.size
                    
                    StaggeredEntrance(visible = visible, index = 1) {
                        TodayJourneyCard(
                            currentPrayer = currentPrayerOf(uiState.prayers),
                            nextPrayer = nextPrayerOf(uiState.prayers),
                            completedCount = completed,
                            totalCount = total,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 3. Prayer Timeline
                    StaggeredEntrance(visible = visible, index = 2) {
                        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
                            Text(
                                text = "Prayer Timeline",
                                style = SGTextStyles.sectionTitle,
                                color = SGColors.textPrimary
                            )
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth().then(SGGlass.standard()),
                                shape = SGShapes.large,
                                color = Color.Transparent
                            ) {
                                Column(modifier = Modifier.padding(SGSpacing.sm)) {
                                    uiState.prayers.forEachIndexed { index, prayer ->
                                        PrayerTimelineItem(
                                            prayer = prayer,
                                            isCurrent = currentPrayerOf(uiState.prayers)?.name == prayer.name,
                                            icon = iconFor(prayer.name),
                                            onClick = { detailPrayer = prayer }
                                        )
                                        if (index < uiState.prayers.lastIndex) {
                                            SGDivider(modifier = Modifier.padding(horizontal = SGSpacing.md))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Daily Progress Section
                    StaggeredEntrance(visible = visible, index = 3) {
                        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
                            Text(
                                text = "Daily Progress",
                                style = SGTextStyles.sectionTitle,
                                color = SGColors.textPrimary
                            )
                            
                            val missed = prayables.count { it.status == PrayerStatus.MISSED }
                            val remaining = (total - completed - missed).coerceAtLeast(0)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(SGSpacing.md)
                            ) {
                                DailyProgressCard(
                                    label = "Completed",
                                    value = completed.toString(),
                                    icon = Icons.Outlined.CheckCircle,
                                    color = SGColors.success,
                                    modifier = Modifier.weight(1f)
                                )
                                DailyProgressCard(
                                    label = "Remaining",
                                    value = remaining.toString(),
                                    icon = Icons.Outlined.Schedule,
                                    color = GoldBright,
                                    modifier = Modifier.weight(1f)
                                )
                                DailyProgressCard(
                                    label = "Missed",
                                    value = missed.toString(),
                                    icon = Icons.Outlined.ErrorOutline,
                                    color = MutedSand,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(SGSpacing.xl))
                }
            }
        }

        detailPrayer?.let { prayer ->
            PrayerDetailBottomSheet(
                prayer = prayer,
                isCurrent = currentPrayerOf(uiState.prayers)?.name == prayer.name,
                onDismiss = { detailPrayer = null },
                onToggleStatus = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.togglePrayerStatus(prayer)
                },
                onSaveReflection = { text, mood ->
                    viewModel.saveReflection(prayer, text, mood)
                    // We no longer set detailPrayer = null here, 
                    // because the sheet handles its own internal flow (Dhikr)
                }
            )
        }

        if (showToolsSheet) {
            PrayerToolsBottomSheet(
                onDismiss = { showToolsSheet = false },
                onNavigateToQibla = {
                    showToolsSheet = false
                    onNavigateToQibla()
                },
                onNavigateToMosques = {
                    showToolsSheet = false
                    onNavigateToMosques()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerToolsBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToMosques: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SGColors.surfaceStandard,
        contentColor = SGColors.textPrimary,
        shape = SGShapes.hero
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SGSpacing.xl, vertical = SGSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(SGSpacing.md)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.xs)) {
                Text(
                    text = "Prayer Tools",
                    style = SGTextStyles.sectionTitle,
                    color = SGColors.textPrimary
                )
                Text(
                    text = "Helpful tools before or during prayer",
                    style = SGTextStyles.body,
                    color = SGColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(SGSpacing.md))

            ToolOptionCard(
                title = "Qibla Direction",
                description = "Find the direction of the Kaaba.",
                icon = Icons.Outlined.Explore,
                onClick = onNavigateToQibla
            )

            ToolOptionCard(
                title = "Nearby Mosques",
                description = "Find nearby mosques and open navigation.",
                icon = Icons.Outlined.Mosque,
                onClick = onNavigateToMosques
            )

            Spacer(modifier = Modifier.height(SGSpacing.lg))
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Close",
                    style = SGTextStyles.label,
                    color = SGColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ToolOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    SGGlassCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SGSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(SGColors.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SGColors.accentBright
                )
            }
            Spacer(modifier = Modifier.width(SGSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = SGTextStyles.cardTitle,
                    color = SGColors.textPrimary
                )
                Text(
                    text = description,
                    style = SGTextStyles.caption,
                    color = SGColors.textSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SGColors.textTertiary
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

@Composable
private fun LocationNotice(message: String) {
    SalahGuardCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        elevation = 0f,
        contentPadding = SGSpacing.default
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(SGSpacing.md))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

private fun currentPrayerOf(prayers: List<Prayer>): Prayer? {
    val now = LocalTime.now()
    return prayers.filter { it.name != PrayerName.SUNRISE }
        .lastOrNull { it.scheduledTime <= now }
}

private fun nextPrayerOf(prayers: List<Prayer>): Prayer? {
    val now = LocalTime.now()
    return prayers.filter { it.name != PrayerName.SUNRISE }
        .firstOrNull { it.scheduledTime > now }
}

private fun iconFor(name: PrayerName): ImageVector = when (name) {
    PrayerName.FAJR -> Icons.Outlined.WbTwilight
    PrayerName.SUNRISE -> Icons.Outlined.WbSunny
    PrayerName.DHUHR -> Icons.Outlined.LightMode
    PrayerName.ASR -> Icons.Outlined.WbCloudy
    PrayerName.MAGHRIB -> Icons.Outlined.Brightness4
    PrayerName.ISHA -> Icons.Outlined.NightsStay
}

private fun infoFor(name: PrayerName): String = when (name) {
    PrayerName.FAJR -> "The dawn prayer, two rak'ahs, offered before sunrise while the sky is still quiet."
    PrayerName.SUNRISE -> "Not a prayer in itself — sunrise marks the close of the Fajr window."
    PrayerName.DHUHR -> "The midday prayer, four rak'ahs, a short pause in the middle of the day."
    PrayerName.ASR -> "The afternoon prayer, four rak'ahs, as the light begins to soften."
    PrayerName.MAGHRIB -> "The sunset prayer, three rak'ahs, offered just after the sun goes down."
    PrayerName.ISHA -> "The night prayer, four rak'ahs, closing the day in stillness."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerDetailBottomSheet(
    prayer: Prayer,
    isCurrent: Boolean,
    onDismiss: () -> Unit,
    onToggleStatus: () -> Unit,
    onSaveReflection: (String, String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isSunrise = prayer.name == PrayerName.SUNRISE
    val isDone = prayer.status == PrayerStatus.COMPLETED || prayer.status == PrayerStatus.RECOVERED
    
    // Internal flow states
    var flowState by remember { 
        mutableStateOf(if (isDone) ReflectionFlowState.REFLECTION else ReflectionFlowState.ACTION) 
    }
    
    // Reflection state
    var reflectionText by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<String?>(null) }
    val moods = listOf("😊 Peaceful", "🤲 Grateful", "💙 Hopeful", "🌱 Motivated", "😔 Struggling")

    // Dhikr state
    val dhikrList = listOf(
        DhikrItem("SubhanAllah", "سُبْحَانَ ٱللَّٰهِ"),
        DhikrItem("Alhamdulillah", "ٱلْحَمْدُ لِلَّٰهِ"),
        DhikrItem("Allahu Akbar", "ٱللَّٰهُ أَكْبَرُ")
    )
    var currentDhikrIndex by remember { mutableStateOf(0) }
    var dhikrCounter by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SGColors.surfaceStandard,
        contentColor = SGColors.textPrimary,
        shape = SGShapes.hero,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = SGSpacing.md, bottom = SGSpacing.sm)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(SGShapes.pill)
                    .background(SGColors.textTertiary.copy(alpha = 0.4f))
            )
        }
    ) {
        AnimatedContent(
            targetState = flowState,
            transitionSpec = {
                (fadeIn(animationSpec = tween(600)) + expandVertically(animationSpec = tween(600)))
                    .togetherWith(fadeOut(animationSpec = tween(400)) + shrinkVertically(animationSpec = tween(400)))
            },
            label = "ReflectionDhikrFlow"
        ) { targetState ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = SGSpacing.xl, vertical = SGSpacing.default),
                verticalArrangement = Arrangement.spacedBy(SGSpacing.lg)
            ) {
                when (targetState) {
                    ReflectionFlowState.ACTION -> {
                        PrayerActionContent(
                            prayer = prayer,
                            isCurrent = isCurrent,
                            onMarkAsPrayed = {
                                onToggleStatus()
                                flowState = ReflectionFlowState.REFLECTION
                            }
                        )
                    }
                    ReflectionFlowState.REFLECTION -> {
                        ReflectionContent(
                            prayer = prayer,
                            text = reflectionText,
                            onTextChange = { reflectionText = it },
                            selectedMood = selectedMood,
                            onMoodChange = { selectedMood = it },
                            moods = moods,
                            onUndo = {
                                onToggleStatus()
                                flowState = ReflectionFlowState.ACTION
                            },
                            onSave = {
                                if (reflectionText.isNotBlank()) {
                                    onSaveReflection(reflectionText, selectedMood)
                                }
                                flowState = ReflectionFlowState.TRANSITION
                            },
                            onSkip = { flowState = ReflectionFlowState.TRANSITION }
                        )
                    }
                    ReflectionFlowState.TRANSITION -> {
                        TransitionContent(
                            onStartDhikr = { flowState = ReflectionFlowState.DHIKR },
                            onNotNow = onDismiss
                        )
                    }
                    ReflectionFlowState.DHIKR -> {
                        val currentItem = dhikrList[currentDhikrIndex]
                        DhikrContent(
                            prayerName = prayer.name.name.capitalizeFirst(),
                            dhikrEnglish = currentItem.english,
                            dhikrArabic = currentItem.arabic,
                            counter = dhikrCounter,
                            onTap = {
                                if (dhikrCounter < 33) {
                                    dhikrCounter++
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            onNext = {
                                if (currentDhikrIndex < dhikrList.size - 1) {
                                    currentDhikrIndex++
                                    dhikrCounter = 0
                                } else {
                                    flowState = ReflectionFlowState.PEACE
                                }
                            },
                            onFinish = { flowState = ReflectionFlowState.PEACE }
                        )
                    }
                    ReflectionFlowState.PEACE -> {
                        PeaceContent(onDone = onDismiss)
                    }
                }
                Spacer(modifier = Modifier.height(SGSpacing.xl))
            }
        }
    }
}

private enum class ReflectionFlowState {
    ACTION, REFLECTION, TRANSITION, DHIKR, PEACE
}

private data class DhikrItem(val english: String, val arabic: String)

@Composable
private fun PrayerActionContent(
    prayer: Prayer,
    isCurrent: Boolean,
    onMarkAsPrayed: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.lg)) {
        PrayerHeader(prayer, isCurrent, false)
        Text(
            text = infoFor(prayer.name),
            style = SGTextStyles.body,
            color = SGColors.textSecondary,
            lineHeight = 22.sp
        )
        if (prayer.name != PrayerName.SUNRISE) {
            SGPrimaryButton(
                text = "MARK AS PRAYED",
                onClick = onMarkAsPrayed,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReflectionContent(
    prayer: Prayer,
    text: String,
    onTextChange: (String) -> Unit,
    selectedMood: String?,
    onMoodChange: (String?) -> Unit,
    moods: List<String>,
    onUndo: () -> Unit,
    onSave: () -> Unit,
    onSkip: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reflection",
                style = SGTextStyles.sectionTitle,
                color = SGColors.textPrimary
            )
            Text(
                text = "Undo",
                style = SGTextStyles.label.copy(color = SGColors.textTertiary),
                modifier = Modifier.clickable { onUndo() }
            )
        }
        
        Text(
            text = "How did this prayer feel today?",
            style = SGTextStyles.body,
            color = SGColors.textSecondary
        )

        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.sm)) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Record your heart's state...", color = SGColors.textTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = SGShapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SGColors.glassBorderSubtle,
                    focusedBorderColor = SGColors.accentBright,
                    focusedTextColor = SGColors.textPrimary,
                    unfocusedTextColor = SGColors.textPrimary
                )
            )

            Text(
                text = "Mood",
                style = SGTextStyles.label,
                color = SGColors.textSecondary,
                modifier = Modifier.padding(top = SGSpacing.xs)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(SGSpacing.sm)
            ) {
                moods.forEach { mood ->
                    SGChip(
                        text = mood,
                        style = if (selectedMood == mood) SGChipStyle.Active else SGChipStyle.Inactive,
                        modifier = Modifier.clickable { onMoodChange(if (selectedMood == mood) null else mood) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = SGSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(SGSpacing.md)
        ) {
            SGSecondaryButton(
                text = "Skip",
                onClick = onSkip,
                modifier = Modifier.weight(1f)
            )
            SGPrimaryButton(
                text = "Save",
                onClick = onSave,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TransitionContent(
    onStartDhikr: () -> Unit,
    onNotNow: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = SGSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SGSpacing.lg)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SGColors.success.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SGColors.success,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(SGSpacing.xs)) {
            Text(
                text = "Prayer Recorded",
                style = SGTextStyles.cardTitle,
                color = SGColors.textPrimary
            )
            Text(
                text = "May Allah accept your prayer.",
                style = SGTextStyles.body,
                color = SGColors.textSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Take one minute to remember Him.",
                style = SGTextStyles.body.copy(fontWeight = FontWeight.Medium),
                color = SGColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SGSpacing.md)
        ) {
            SGPrimaryButton(
                text = "Start Dhikr",
                onClick = onStartDhikr,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            TextButton(onClick = onNotNow) {
                Text(
                    text = "Not now",
                    style = SGTextStyles.label,
                    color = SGColors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun DhikrContent(
    prayerName: String,
    dhikrEnglish: String,
    dhikrArabic: String,
    counter: Int,
    onTap: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = counter / 33f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "DhikrProgress"
    )
    
    val animatedCounter by animateIntAsState(
        targetValue = counter,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "CounterAnimation"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SGSpacing.xl)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(SGSpacing.xs)) {
            Text(
                text = prayerName,
                style = SGTextStyles.label,
                color = SGColors.accent
            )
            Text(
                text = dhikrArabic,
                style = SGTextStyles.heroTitle.copy(fontSize = 32.sp),
                color = SGColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = dhikrEnglish,
                style = SGTextStyles.body.copy(fontWeight = FontWeight.Medium),
                color = SGColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(color = SGColors.accent),
                    onClick = onTap
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(SGSpacing.md)) {
                // Background Track
                drawCircle(
                    color = SGColors.progressTrack.copy(alpha = 0.2f),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                // Progress
                drawArc(
                    color = SGColors.accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = animatedCounter.toString(),
                    style = SGTextStyles.heroTitle.copy(fontSize = 64.sp, fontWeight = FontWeight.Bold),
                    color = SGColors.textPrimary
                )
                Text(
                    text = "/ 33",
                    style = SGTextStyles.body,
                    color = SGColors.textSecondary
                )
            }
        }

        AnimatedVisibility(
            visible = counter >= 33,
            enter = fadeIn() + expandVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SGSpacing.md)
            ) {
                Text(
                    text = "$dhikrEnglish Complete",
                    style = SGTextStyles.label,
                    color = SGColors.success
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SGSpacing.md)
                ) {
                    SGSecondaryButton(
                        text = "Finish Session",
                        onClick = onFinish,
                        modifier = Modifier.weight(1f)
                    )
                    SGPrimaryButton(
                        text = "Continue",
                        onClick = onNext,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PeaceContent(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = SGSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SGSpacing.lg)
    ) {
        Text(
            text = "🤍",
            fontSize = 48.sp
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(SGSpacing.xs)) {
            Text(
                text = "Your heart remembered Allah.",
                style = SGTextStyles.cardTitle,
                color = SGColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "May Allah accept your worship.\nToday's remembrance has been recorded.",
                style = SGTextStyles.body,
                color = SGColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        SGPrimaryButton(
            text = "Done",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}

@Composable
private fun PrayerHeader(
    prayer: Prayer,
    isCurrent: Boolean,
    isDone: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(SGColors.accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconFor(prayer.name),
                contentDescription = null,
                tint = if (isCurrent) SGColors.accentBright else SGColors.accent,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(SGSpacing.default))
        Column {
            Text(
                text = prayer.name.name.lowercase().replaceFirstChar { it.uppercase() },
                style = SGTextStyles.sectionTitle,
                color = SGColors.textPrimary
            )
            Text(
                text = prayer.scheduledTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                style = SGTextStyles.body,
                color = SGColors.textSecondary
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        
        val statusText = when {
            isDone -> "Completed"
            isCurrent -> "Current"
            prayer.status == PrayerStatus.MISSED -> "Missed"
            else -> "Upcoming"
        }
        val statusColor = when {
            isDone -> SGColors.success
            isCurrent -> SGColors.accentBright
            else -> SGColors.textSecondary
        }
        Surface(color = statusColor.copy(alpha = 0.15f), shape = SGShapes.pill) {
            Text(
                text = statusText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = SGTextStyles.label.copy(color = statusColor, fontWeight = FontWeight.Bold)
            )
        }
    }
}

