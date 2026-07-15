package com.salahguard.app.presentation.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salahguard.app.presentation.components.HomeSanctuaryBackground
import com.salahguard.app.presentation.components.SalahGuardCard
import com.salahguard.app.presentation.designsystem.*
import com.salahguard.app.presentation.designsystem.components.*
import com.salahguard.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    var totalDrag by remember { mutableFloatStateOf(0f) }
    
    val swipeScale by animateFloatAsState(
        targetValue = if (totalDrag != 0f) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "swipeScale"
    )
    
    var selectedPrayerForSheet by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.checkPermission()
    }

    HomeSanctuaryBackground(currentPrayerName = uiState.currentPrayerName) {
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
                                onNavigateToSettings()
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
                            "NOTIFICATIONS", 
                            style = SGTextStyles.label.copy(letterSpacing = 4.sp, fontWeight = FontWeight.Bold),
                            color = SGColors.textPrimary
                        ) 
                    },
                    navigationIcon = {
                        SGIconButton(
                            icon = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            onClick = onNavigateBack,
                            modifier = Modifier.padding(start = SGSpacing.md),
                            size = SGDimensions.iconButtonSizeCompact
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = SGSpacing.xl, vertical = SGSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(SGSpacing.xxl)
            ) {
                // Background Restrictions Warning
                AnimatedVisibility(
                    visible = !uiState.isBatteryOptimizationIgnored,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    PermissionCard(
                        title = "Alarm Reliability",
                        description = "Enable unrestricted battery usage to ensure prayer alarms sound while the screen is off.",
                        onGrantClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            intent.data = android.net.Uri.parse("package:${context.packageName}")
                            context.startActivity(intent)
                        }
                    )
                }

                // Section 1: Prayer Reminders Hero Card
                PrayerRemindersHeroCard(
                    isEnabled = uiState.isNotificationsEnabled,
                    onToggle = { viewModel.toggleNotifications(it) }
                )

                // Progressive Disclosure: Only show settings if master toggle is ON
                AnimatedVisibility(
                    visible = uiState.isNotificationsEnabled,
                    enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.xxl)) {
                        
                        // Section 2: Global Settings (Reminder Timing & Sound)
                        GlobalSettingsSection(
                            reminderOffset = uiState.reminderTimeOffset,
                            onOffsetSelected = { viewModel.setReminderTimeOffset(it) },
                            alarmSound = uiState.alarmSound,
                            onSoundSelected = { viewModel.setAlarmSound(it) }
                        )

                        // Section 3: Extra Fajr Reminder
                        ExtraFajrReminderCard(
                            isEnabled = uiState.isExtraFajrReminderEnabled,
                            onToggle = { viewModel.toggleExtraFajrReminder(it) },
                            selectedOffset = uiState.extraFajrOffset,
                            onOffsetSelected = { viewModel.setExtraFajrOffset(it) }
                        )

                        // Section 4: Individual Prayers
                        IndividualPrayersSection(
                            prayerNotifications = uiState.prayerNotifications,
                            onPrayerClick = { selectedPrayerForSheet = it }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(SGSpacing.xxl))
            }
        }

        if (selectedPrayerForSheet != null) {
            val prayer = selectedPrayerForSheet!!
            ModalBottomSheet(
                onDismissRequest = { selectedPrayerForSheet = null },
                sheetState = sheetState,
                containerColor = NightBackground,
                dragHandle = { BottomSheetDefaults.DragHandle(color = SGColors.glassBorderSubtle) },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                PrayerCustomizationContent(
                    prayerName = prayer,
                    isNotificationEnabled = uiState.prayerNotifications[prayer] ?: true,
                    isAlarmEnabled = uiState.prayerAlarms[prayer] ?: false,
                    reminderOffset = uiState.reminderTimeOffset,
                    alarmSound = uiState.alarmSound,
                    onNotificationToggle = { viewModel.togglePrayerNotification(prayer, it) },
                    onAlarmToggle = { viewModel.togglePrayerAlarm(prayer, it) },
                    onOffsetSelected = { viewModel.setReminderTimeOffset(it) },
                    onSoundSelected = { viewModel.setAlarmSound(it) },
                    onClose = { selectedPrayerForSheet = null }
                )
            }
        }
    }
}

@Composable
private fun PrayerRemindersHeroCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (isEnabled) SGElevation.hero else SGElevation.raised,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "heroElevation"
    )

    SGHeroCard(elevation = elevation) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = SGSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Prayer Reminders",
                    style = SGTextStyles.heroTitle.copy(fontSize = 24.sp),
                    color = SGColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stay gently reminded of your connection.",
                    style = SGTextStyles.body.copy(fontSize = 14.sp),
                    color = SGColors.textSecondary
                )
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NightBackground,
                    checkedTrackColor = Gold,
                    uncheckedThumbColor = SGColors.textTertiary.copy(alpha = 0.6f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.08f),
                    uncheckedBorderColor = Color.Transparent
                ),
                modifier = Modifier.scale(1.1f)
            )
        }
    }
}

@Composable
private fun IndividualPrayersSection(
    prayerNotifications: Map<String, Boolean>,
    onPrayerClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.lg)) {
        SGSectionTitle(
            title = "Daily Prayers",
            subtitle = "Custom alerts for each time"
        )
        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
            val prayers = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
            prayers.forEach { prayer ->
                PrayerCard(
                    name = prayer,
                    isEnabled = prayerNotifications[prayer] ?: true,
                    onClick = { onPrayerClick(prayer) }
                )
            }
        }
    }
}

@Composable
private fun PrayerCard(
    name: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val icon = when(name) {
        "FAJR" -> Icons.Default.NightsStay
        "DHUHR" -> Icons.Default.WbSunny
        "ASR" -> Icons.Default.WbCloudy
        "MAGHRIB" -> Icons.Default.WbTwilight
        "ISHA" -> Icons.Default.Bedtime
        else -> Icons.Default.Notifications
    }

    SGGlassCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(SGShapes.medium)
                    .background(if (isEnabled) Gold.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                val iconTint by animateColorAsState(
                    targetValue = if (isEnabled) GoldBright else SGColors.textTertiary,
                    animationSpec = tween(400)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(SGSpacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.lowercase().replaceFirstChar { it.uppercase() },
                    style = SGTextStyles.cardTitle.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                    color = SGColors.textPrimary
                )
                Text(
                    text = if (isEnabled) "Active" else "Muted",
                    style = SGTextStyles.caption.copy(fontSize = 13.sp),
                    color = if (isEnabled) Gold.copy(alpha = 0.8f) else SGColors.textTertiary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SGColors.textTertiary.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun GlobalSettingsSection(
    reminderOffset: Int,
    onOffsetSelected: (Int) -> Unit,
    alarmSound: String,
    onSoundSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.xxl)) {
        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.lg)) {
            SGSectionTitle(title = "Default Timing")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(SGSpacing.sm)
            ) {
                val options = listOf(0, 5, 10, 15)
                options.forEach { minutes ->
                    ChoiceChip(
                        label = if (minutes == 0) "On Time" else "$minutes min before",
                        isSelected = reminderOffset == minutes,
                        onClick = { onOffsetSelected(minutes) }
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.lg)) {
            SGSectionTitle(title = "Default Tone")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(SGSpacing.md)
            ) {
                val sounds = listOf("DEFAULT", "SOFT", "CHIME", "SILENT")
                val labels = mapOf("DEFAULT" to "Classic", "SOFT" to "Soft Adhan", "CHIME" to "Gentle", "SILENT" to "Silent")
                sounds.forEach { sound ->
                    SoundChip(
                        label = labels[sound] ?: sound,
                        isSelected = alarmSound == sound,
                        onClick = { onSoundSelected(sound) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtraFajrReminderCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    selectedOffset: Int,
    onOffsetSelected: (Int) -> Unit
) {
    SGGlassCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(if (isEnabled) Gold.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.04f)),
                    contentAlignment = Alignment.Center
                ) {
                    val iconTint by animateColorAsState(targetValue = if (isEnabled) GoldBright else SGColors.textTertiary, animationSpec = tween(400))
                    Icon(Icons.Default.NightsStay, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(SGSpacing.lg))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Extra Fajr Reminder", style = SGTextStyles.cardTitle.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp), color = SGColors.textPrimary)
                    Text("Gentle wake-up call before dawn.", style = SGTextStyles.caption, color = SGColors.textSecondary)
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NightBackground,
                        checkedTrackColor = Gold
                    )
                )
            }
            
            AnimatedVisibility(
                visible = isEnabled,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(SGSpacing.xl))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(SGSpacing.sm)
                    ) {
                        listOf(10, 15, 20, 30).forEach { minutes ->
                            ChoiceChip(
                                label = "$minutes min before",
                                isSelected = selectedOffset == minutes,
                                onClick = { onOffsetSelected(minutes) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val duration = 300
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Gold.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(duration),
        label = "chipBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) GoldBright else SGColors.textSecondary,
        animationSpec = tween(duration),
        label = "chipContent"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Gold.copy(alpha = 0.4f) else SGColors.glassBorderSubtle,
        animationSpec = tween(duration),
        label = "chipBorder"
    )

    Surface(
        modifier = Modifier
            .clip(SGShapes.pill)
            .clickable { onClick() },
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = SGShapes.pill
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = SGTextStyles.label.copy(
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 0.sp,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun SoundChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val duration = 300
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Gold.copy(alpha = 0.1f) else SGColors.glassFillSubtle,
        animationSpec = tween(duration),
        label = "soundChipBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) GoldBright else SGColors.textSecondary,
        animationSpec = tween(duration),
        label = "soundChipContent"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Gold.copy(alpha = 0.4f) else Color.Transparent,
        animationSpec = tween(duration),
        label = "soundChipBorder"
    )

    Surface(
        modifier = Modifier
            .width(105.dp)
            .height(64.dp)
            .clip(SGShapes.large)
            .clickable { onClick() },
        color = backgroundColor,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null,
        shape = SGShapes.large
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(SGSpacing.sm)) {
            Text(
                text = label,
                style = SGTextStyles.label.copy(
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    letterSpacing = 0.sp,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun PrayerCustomizationContent(
    prayerName: String,
    isNotificationEnabled: Boolean,
    isAlarmEnabled: Boolean,
    reminderOffset: Int,
    alarmSound: String,
    onNotificationToggle: (Boolean) -> Unit,
    onAlarmToggle: (Boolean) -> Unit,
    onOffsetSelected: (Int) -> Unit,
    onSoundSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(SGSpacing.xxl)
    ) {
        Column {
            Text(
                text = prayerName.lowercase().replaceFirstChar { it.uppercase() },
                style = SGTextStyles.heroTitle.copy(fontSize = 28.sp),
                color = WarmIvory
            )
            Text(
                text = "Preferences for this prayer time",
                style = SGTextStyles.body.copy(color = SGColors.textSecondary)
            )
        }
        
        SGGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                CustomizationOption(
                    title = "Reminder",
                    subtitle = "Notification $reminderOffset min before",
                    isEnabled = isNotificationEnabled,
                    onToggle = onNotificationToggle
                )
                
                SGDivider()
                
                CustomizationOption(
                    title = "Alarm",
                    subtitle = "High priority audible alert",
                    isEnabled = isAlarmEnabled,
                    onToggle = onAlarmToggle
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Timing", style = SGTextStyles.label.copy(color = SGColors.textTertiary))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(SGSpacing.sm)
            ) {
                listOf(0, 5, 10, 15).forEach { minutes ->
                    ChoiceChip(
                        label = if (minutes == 0) "On Time" else "$minutes min",
                        isSelected = reminderOffset == minutes,
                        onClick = { onOffsetSelected(minutes) }
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Sound", style = SGTextStyles.label.copy(color = SGColors.textTertiary))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(SGSpacing.md)
            ) {
                val sounds = listOf("DEFAULT", "SOFT", "CHIME", "SILENT")
                val labels = mapOf("DEFAULT" to "Classic", "SOFT" to "Soft", "CHIME" to "Gentle", "SILENT" to "Silent")
                sounds.forEach { sound ->
                    ChoiceChip(
                        label = labels[sound] ?: sound,
                        isSelected = alarmSound == sound,
                        onClick = { onSoundSelected(sound) }
                    )
                }
            }
        }
        
        SGPrimaryButton(
            text = "Apply Settings",
            onClick = onClose,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun CustomizationOption(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = SGTextStyles.body.copy(fontWeight = FontWeight.Bold), color = WarmIvory)
            Text(subtitle, style = SGTextStyles.caption, color = SageMist)
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NightBackground,
                checkedTrackColor = Gold
            )
        )
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    onGrantClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SGColors.glassFill,
        shape = SGShapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(SGSpacing.lg),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Gold.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(SGSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = SGTextStyles.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    color = SGColors.textPrimary
                )
                Text(
                    text = description,
                    style = SGTextStyles.caption,
                    color = SGColors.textSecondary
                )
                Spacer(modifier = Modifier.height(SGSpacing.md))
                Button(
                    onClick = onGrantClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold.copy(alpha = 0.1f), 
                        contentColor = GoldBright
                    ),
                    shape = SGShapes.pill,
                    contentPadding = PaddingValues(horizontal = SGSpacing.lg, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Settings", style = SGTextStyles.label.copy(fontSize = 11.sp))
                }
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    graphicsLayer(scaleX = scale, scaleY = scale)
)
