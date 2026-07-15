package com.salahguard.app.presentation.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
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
fun SalahGuardSettingsScreen(
    viewModel: SalahGuardSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToNotifications: () -> Unit
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
                            if (totalDrag < -140) { // Swipe Left
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToNotifications()
                            } else if (totalDrag > 140) { // Swipe Right
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateBack()
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
                            "PROTECTION", 
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
                // Hero Card
                ProtectionHeroCard()

                AnimatedVisibility(
                    visible = !uiState.isBatteryOptimizationIgnored,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    PermissionCard(
                        title = "Background Connectivity",
                        description = "Enable 'Unrestricted' battery usage to ensure protection activates reliably.",
                        onGrantClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            intent.data = android.net.Uri.parse("package:${context.packageName}")
                            context.startActivity(intent)
                        }
                    )
                }

                AnimatedVisibility(
                    visible = !uiState.hasDndPermission && uiState.selectedMode == "DND",
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    PermissionCard(
                        title = "DND Access Required",
                        description = "Allow SalahGuard to manage Do Not Disturb modes automatically.",
                        onGrantClick = {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            context.startActivity(intent)
                        }
                    )
                }

                // Protection Modes Section
                Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.lg)) {
                    SGSectionTitle(
                        title = "Sanctuary Mode",
                        subtitle = "How your phone behaves during prayer"
                    )
                    
                    val modes = listOf(
                        ModeItem("DISABLED", "Off", Icons.Default.NotificationsActive, "No automatic changes."),
                        ModeItem("SILENT", "Silent", Icons.Default.NotificationsOff, "Mute all sounds."),
                        ModeItem("VIBRATE", "Vibrate", Icons.Default.Vibration, "Subtle haptic alerts only."),
                        ModeItem("DND", "Focus", Icons.Default.DoNotDisturbOn, "Block calls and notifications.")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
                        modes.forEach { mode ->
                            ProtectionModeCard(
                                item = mode,
                                isSelected = uiState.selectedMode == mode.id,
                                onClick = { viewModel.setMode(mode.id) }
                            )
                        }
                    }
                }

                // Focus Mode Card (Premium)
                FocusModePremiumCard(
                    isEnabled = uiState.isFocusModeEnabled,
                    onToggle = { viewModel.toggleFocusMode(it) }
                )

                // Advanced Settings Section (Brightness & Timeout)
                AdvancedSettingsSection(
                    isBrightnessDimEnabled = uiState.isBrightnessDimEnabled,
                    onBrightnessDimToggle = { viewModel.toggleBrightnessDim(it) },
                    isScreenTimeoutEnabled = uiState.isScreenTimeoutEnabled,
                    onScreenTimeoutToggle = { viewModel.toggleScreenTimeout(it) },
                    hasPermission = uiState.hasWriteSettingsPermission,
                    onGrantPermission = {
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                        intent.data = android.net.Uri.parse("package:${context.packageName}")
                        context.startActivity(intent)
                    }
                )

                // Bottom Footer Card
                ProtectionFooterCard()
                
                Spacer(modifier = Modifier.height(SGSpacing.xxl))
            }
        }
    }
}

@Composable
private fun ProtectionHeroCard() {
    SGHeroCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = SGSpacing.md)
        ) {
            Box(contentAlignment = Alignment.Center) {
                val infiniteTransition = rememberInfiniteTransition(label = "shieldGlow")
                val glowScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glowScale"
                )
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            scaleX = glowScale
                            scaleY = glowScale
                        }
                        .background(Gold.copy(alpha = 0.08f), CircleShape)
                        .shadow(48.dp, CircleShape, spotColor = Gold.copy(alpha = 0.3f))
                )
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = GoldBright,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(modifier = Modifier.height(SGSpacing.xl))
            Text(
                text = "SalahGuard Protection",
                style = SGTextStyles.heroTitle.copy(fontSize = 26.sp, letterSpacing = (-0.5).sp),
                color = SGColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(SGSpacing.xs))
            Text(
                text = "Your phone quietly protects your prayer while you focus on Allah.",
                style = SGTextStyles.body.copy(fontSize = 14.sp, lineHeight = 22.sp),
                color = SGColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = SGSpacing.lg)
            )
        }
    }
}

@Composable
private fun ProtectionModeCard(
    item: ModeItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val duration = 400
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Gold.copy(alpha = 0.6f) else Color.Transparent, 
        animationSpec = tween(duration),
        label = "borderColor"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) SGColors.glassFillRaised else SGColors.glassFill,
        animationSpec = tween(duration),
        label = "containerColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.01f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(SGShapes.large)
            .border(1.dp, borderColor, SGShapes.large)
            .clickable { onClick() }
            .drawBehind {
                if (isSelected) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Gold.copy(alpha = 0.08f), Color.Transparent),
                            center = center,
                            radius = size.width
                        )
                    )
                }
            },
        color = containerColor,
        shape = SGShapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SGSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(SGShapes.medium)
                    .background(if (isSelected) Gold.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) GoldBright else SGColors.textTertiary,
                    animationSpec = tween(duration)
                )
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(SGSpacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    style = SGTextStyles.cardTitle.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                    color = if (isSelected) SGColors.textPrimary else SGColors.textPrimary.copy(alpha = 0.9f)
                )
                Text(
                    text = item.description,
                    style = SGTextStyles.caption.copy(fontSize = 13.sp),
                    color = if (isSelected) SGColors.textSecondary else SGColors.textTertiary
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = GoldBright,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun FocusModePremiumCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    SGGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = SGSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isEnabled) Gold.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.04f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isEnabled) GoldBright else SGColors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(SGSpacing.lg))
                Column {
                    Text(
                        text = "Focus Mode", 
                        style = SGTextStyles.cardTitle.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp), 
                        color = SGColors.textPrimary
                    )
                    Text(
                        text = "Diminish distractions during prayer.", 
                        style = SGTextStyles.caption, 
                        color = SGColors.textSecondary
                    )
                }
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
                )
            )
        }
    }
}

@Composable
private fun AdvancedSettingsSection(
    isBrightnessDimEnabled: Boolean,
    onBrightnessDimToggle: (Boolean) -> Unit,
    isScreenTimeoutEnabled: Boolean,
    onScreenTimeoutToggle: (Boolean) -> Unit,
    hasPermission: Boolean,
    onGrantPermission: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.lg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = SGSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Screen Optimization", 
                style = SGTextStyles.label.copy(color = SGColors.textTertiary)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = SGColors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
                if (!hasPermission && (isBrightnessDimEnabled || isScreenTimeoutEnabled)) {
                    PermissionCard(
                        title = "System Permission",
                        description = "Allow modifying settings to optimize screen during prayer.",
                        onGrantClick = onGrantPermission
                    )
                }

                AdvancedSettingToggle(
                    title = "Brightness Dim",
                    subtitle = "Lower brightness automatically",
                    icon = Icons.Default.BrightnessMedium,
                    isEnabled = isBrightnessDimEnabled,
                    onToggle = onBrightnessDimToggle
                )

                AdvancedSettingToggle(
                    title = "Screen Timeout",
                    subtitle = "Reduce timeout to 15 seconds",
                    icon = Icons.Default.Timer,
                    isEnabled = isScreenTimeoutEnabled,
                    onToggle = onScreenTimeoutToggle
                )
            }
        }
    }
}

@Composable
private fun AdvancedSettingToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val duration = 400
    val containerColor by animateColorAsState(
        targetValue = if (isEnabled) SGColors.glassFillRaised else SGColors.glassFillSubtle,
        animationSpec = tween(duration)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SGShapes.medium,
        color = containerColor,
        border = if (isEnabled) null else androidx.compose.foundation.BorderStroke(1.dp, SGColors.glassBorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(SGSpacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) GoldBright else SGColors.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(SGSpacing.lg))
                Column {
                    Text(title, style = SGTextStyles.body.collect(fontWeight = FontWeight.Bold), color = SGColors.textPrimary)
                    Text(subtitle, style = SGTextStyles.caption, color = SGColors.textSecondary)
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NightBackground,
                    checkedTrackColor = Gold.copy(alpha = 0.8f),
                    uncheckedThumbColor = SGColors.textTertiary.copy(alpha = 0.4f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.05f),
                    uncheckedBorderColor = Color.Transparent
                ),
                modifier = Modifier.scale(0.9f)
            )
        }
    }
}

private fun TextStyle.collect(fontWeight: FontWeight): TextStyle = this.copy(fontWeight = fontWeight)

@Composable
private fun ProtectionFooterCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = SGSpacing.md),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(SGShapes.pill)
                .background(Color.White.copy(alpha = 0.03f))
                .padding(horizontal = SGSpacing.xl, vertical = SGSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome, 
                contentDescription = null, 
                tint = Gold.copy(alpha = 0.5f), 
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(SGSpacing.md))
            Text(
                text = "Activates automatically during prayer window",
                style = SGTextStyles.caption.copy(fontSize = 12.sp, letterSpacing = 0.sp),
                color = SGColors.textTertiary
            )
        }
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
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(SGSpacing.lg),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
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
                        containerColor = Gold.copy(alpha = 0.15f), 
                        contentColor = GoldBright
                    ),
                    shape = SGShapes.pill,
                    contentPadding = PaddingValues(horizontal = SGSpacing.lg, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Grant Permission", style = SGTextStyles.label.copy(fontSize = 11.sp))
                }
            }
        }
    }
}

data class ModeItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val description: String
)

private fun Modifier.scale(scale: Float): Modifier = this.then(
    graphicsLayer(scaleX = scale, scaleY = scale)
)
