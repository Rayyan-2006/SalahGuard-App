package com.salahguard.app.presentation.screens.reflection

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.salahguard.app.domain.model.Reflection
import com.salahguard.app.presentation.components.*
import com.salahguard.app.presentation.designsystem.*
import com.salahguard.app.presentation.designsystem.components.*
import com.salahguard.app.presentation.screens.home.HomeViewModel
import com.salahguard.app.presentation.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ReflectionScreen(
    viewModel: ReflectionViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    initialQuery: String? = null,
    onNavigateToHome: () -> Unit,
    onNavigateToPrayers: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToJourney: () -> Unit,
    onNavigateToReflection: (String?) -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToMosques: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(initialQuery) {
        if (initialQuery != null) {
            viewModel.onSearchQueryChange(initialQuery)
        }
    }
    
    // Editor State
    var isEditorOpen by remember { mutableStateOf(false) }
    var editingReflection by remember { mutableStateOf<Reflection?>(null) }
    var editorText by remember { mutableStateOf("") }
    var editorPrayer by remember { mutableStateOf(homeUiState.currentPrayerName ?: "General") }
    var editorMood by remember { mutableStateOf<String?>(null) }

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
                    if (!isEditorOpen) {
                        scaleX = swipeScale
                        scaleY = swipeScale
                        translationX = totalDrag * 0.4f
                        alpha = (1f - (kotlin.math.abs(totalDrag) / 1000f)).coerceIn(0.7f, 1f)
                    }
                }
                .pointerInput(isEditorOpen) {
                    if (!isEditorOpen) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                            },
                            onDragEnd = {
                                if (totalDrag > 140) { // Swipe Right
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onNavigateToJourney()
                                }
                                totalDrag = 0f
                            },
                            onDragCancel = { totalDrag = 0f }
                        )
                    }
                },
            bottomBar = {
                if (!isEditorOpen) {
                    SalahGuardBottomNavBar(
                        selected = BottomNavDestination.REFLECTION,
                        onSelect = {
                            when (it) {
                                BottomNavDestination.HOME -> onNavigateToHome()
                                BottomNavDestination.PRAYERS -> onNavigateToPrayers()
                                BottomNavDestination.LEARN -> onNavigateToQuran()
                                BottomNavDestination.JOURNEY -> onNavigateToJourney()
                                BottomNavDestination.REFLECTION -> onNavigateToReflection(null)
                                else -> {}
                            }
                        }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = SGSpacing.lg, vertical = SGSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(SGSpacing.xl)
                ) {
                    // 1. Header
                    item {
                        StaggeredEntrance(visible = visible, index = 0) {
                            Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.xs)) {
                                Text(
                                    text = "Reflection",
                                    style = SGTextStyles.heroTitle,
                                    color = SGColors.textPrimary
                                )
                                Text(
                                    text = "Take a quiet moment after prayer.",
                                    style = SGTextStyles.body.copy(
                                        color = SGColors.textSecondary.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    if (uiState.reflections.isEmpty() && !uiState.isLoading) {
                        // 6. Empty State
                        item {
                            StaggeredEntrance(visible = visible, index = 1) {
                                ReflectionEmptyState(onStartClick = {
                                    editorText = ""
                                    editorPrayer = homeUiState.currentPrayerName ?: "General"
                                    editorMood = null
                                    editingReflection = null
                                    isEditorOpen = true
                                })
                            }
                        }
                    } else {
                        // 2. Today's Reflection Hero
                        item {
                            val todayReflection = uiState.reflections.find { it.date == LocalDate.now() }
                            StaggeredEntrance(visible = visible, index = 1) {
                                TodayReflectionHero(
                                    reflection = todayReflection,
                                    onActionClick = {
                                        if (todayReflection != null) {
                                            editingReflection = todayReflection
                                            editorText = todayReflection.reflectionText
                                            editorPrayer = todayReflection.prayerName
                                            editorMood = todayReflection.mood
                                        } else {
                                            editorText = ""
                                            editorPrayer = homeUiState.currentPrayerName ?: "General"
                                            editorMood = null
                                        }
                                        isEditorOpen = true
                                    }
                                )
                            }
                        }

                        // 3. Quick Reflection Prompts
                        item {
                            StaggeredEntrance(visible = visible, index = 2) {
                                QuickReflectionPrompts(onPromptClick = { prompt ->
                                    editorText = prompt + "\n\n"
                                    editorPrayer = homeUiState.currentPrayerName ?: "General"
                                    editorMood = null
                                    editingReflection = null
                                    isEditorOpen = true
                                })
                            }
                        }

                        // 4. Search & Filter
                        item {
                            StaggeredEntrance(visible = visible, index = 3) {
                                Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
                                    ReflectionSearchBar(
                                        query = uiState.searchQuery,
                                        onQueryChange = { viewModel.onSearchQueryChange(it) }
                                    )
                                    ReflectionFilters(
                                        selectedFilter = uiState.selectedPrayerFilter,
                                        onFilterClick = { viewModel.onPrayerFilterChange(it) }
                                    )
                                }
                            }
                        }

                        // 5. Reflection Timeline
                        if (uiState.filteredReflections.isEmpty()) {
                            item {
                                Text(
                                    text = "No reflections found matching your criteria.",
                                    style = SGTextStyles.body,
                                    color = SGColors.textTertiary,
                                    modifier = Modifier.padding(vertical = SGSpacing.xl)
                                )
                            }
                        } else {
                            items(uiState.filteredReflections) { reflection ->
                                StaggeredEntrance(visible = visible, index = 4) {
                                    ReflectionTimelineItem(
                                        reflection = reflection,
                                        onClick = {
                                            editingReflection = reflection
                                            editorText = reflection.reflectionText
                                            editorPrayer = reflection.prayerName
                                            editorMood = reflection.mood
                                            isEditorOpen = true
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(SGSpacing.xl)) }
                }

                // 7. Reflection Editor Overlay
                AnimatedVisibility(
                    visible = isEditorOpen,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    ReflectionEditor(
                        prayerName = editorPrayer,
                        text = editorText,
                        mood = editorMood,
                        onTextChange = { editorText = it },
                        onMoodChange = { editorMood = it },
                        onClose = { 
                            isEditorOpen = false
                            editingReflection = null
                        },
                        onSave = {
                            viewModel.saveReflection(
                                prayerName = editorPrayer,
                                text = editorText,
                                mood = editorMood,
                                id = editingReflection?.id ?: 0L
                            ) {
                                isEditorOpen = false
                                editingReflection = null
                            }
                        },
                        onDelete = {
                            editingReflection?.let {
                                viewModel.deleteReflection(it.id)
                            }
                            isEditorOpen = false
                            editingReflection = null
                        },
                        showDelete = editingReflection != null
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayReflectionHero(
    reflection: Reflection?,
    onActionClick: () -> Unit
) {
    SGHeroCard {
        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                    style = SGTextStyles.label,
                    color = SGColors.accent
                )
                if (reflection != null) {
                    SGChip(text = "Completed", style = SGChipStyle.Success)
                } else {
                    SGChip(text = "Pending", style = SGChipStyle.Inactive)
                }
            }

            Text(
                text = if (reflection != null) "“${reflection.reflectionText.take(60)}...”" else "Nothing written today.",
                style = SGTextStyles.cardTitle.copy(fontSize = 20.sp, fontStyle = if (reflection != null) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal),
                color = SGColors.textPrimary,
                maxLines = 2
            )

            if (reflection != null) {
                Text(
                    text = "Reflected after ${reflection.prayerName}",
                    style = SGTextStyles.caption,
                    color = SGColors.textSecondary
                )
            }

            SGPrimaryButton(
                text = if (reflection != null) "CONTINUE WRITING" else "START REFLECTION",
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuickReflectionPrompts(onPromptClick: (String) -> Unit) {
    val prompts = listOf(
        "What are you grateful for today?",
        "What helped you focus in prayer?",
        "What distracted your heart today?",
        "What blessing did you notice?",
        "What would you like to improve tomorrow?"
    )

    Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
        SGSectionTitle(title = "Prompts")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(SGSpacing.md),
            contentPadding = PaddingValues(end = SGSpacing.lg)
        ) {
            items(prompts) { prompt ->
                SGGlassCard(
                    modifier = Modifier.width(220.dp),
                    onClick = { onPromptClick(prompt) }
                ) {
                    Text(
                        text = prompt,
                        style = SGTextStyles.body.copy(fontWeight = FontWeight.Medium),
                        color = SGColors.textPrimary,
                        minLines = 3
                    )
                }
            }
        }
    }
}

@Composable
private fun ReflectionSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .then(SGGlass.subtle()),
        placeholder = { Text("Search your heart...", style = SGTextStyles.body, color = SGColors.textTertiary) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SGColors.textTertiary) },
        shape = SGShapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = SGColors.textPrimary,
            unfocusedTextColor = SGColors.textPrimary,
            cursorColor = SGColors.accent
        ),
        singleLine = true
    )
}

@Composable
private fun ReflectionFilters(selectedFilter: String?, onFilterClick: (String?) -> Unit) {
    val filters = listOf("All", "Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(SGSpacing.sm)
    ) {
        items(filters) { filter ->
            val isSelected = (filter == "All" && selectedFilter == null) || (filter == selectedFilter)
            SGChip(
                text = filter,
                style = if (isSelected) SGChipStyle.Active else SGChipStyle.Inactive,
                modifier = Modifier.clickable { 
                    onFilterClick(if (filter == "All") null else filter)
                }
            )
        }
    }
}

@Composable
private fun ReflectionTimelineItem(reflection: Reflection, onClick: () -> Unit) {
    SGGlassCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SGSpacing.sm)) {
                    Text(
                        text = reflection.prayerName,
                        style = SGTextStyles.label,
                        color = SGColors.accent
                    )
                    reflection.mood?.let {
                        Text(text = it.take(2), fontSize = 14.sp) // Just show the emoji
                    }
                }
                Text(
                    text = reflection.date.format(DateTimeFormatter.ofPattern("MMM d")),
                    style = SGTextStyles.caption,
                    color = SGColors.textTertiary
                )
            }
            
            Text(
                text = reflection.reflectionText,
                style = SGTextStyles.body,
                color = SGColors.textPrimary,
                maxLines = 3
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Read More",
                    style = SGTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                    color = SGColors.accent
                )
            }
        }
    }
}

@Composable
private fun ReflectionEmptyState(onStartClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SGSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SGSpacing.lg)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(SGColors.accent.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Create,
                contentDescription = null,
                tint = SGColors.accent.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(SGSpacing.xs)) {
            Text(
                text = "Your reflections will begin appearing here.",
                style = SGTextStyles.cardTitle,
                color = SGColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Capture a peaceful moment after your prayer.",
                style = SGTextStyles.body,
                color = SGColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        SGPrimaryButton(
            text = "WRITE YOUR FIRST REFLECTION",
            onClick = onStartClick,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

@Composable
private fun ReflectionEditor(
    prayerName: String,
    text: String,
    mood: String?,
    onTextChange: (String) -> Unit,
    onMoodChange: (String?) -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    showDelete: Boolean = false
) {
    val moods = listOf("😊 Peaceful", "🤲 Grateful", "💙 Hopeful", "🌱 Motivated", "😔 Struggling")
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NightBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(SGSpacing.xl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SGColors.textPrimary)
                }
                Text(
                    text = "Reflecting on $prayerName",
                    style = SGTextStyles.sectionTitle,
                    color = SGColors.textPrimary
                )
                Row {
                    if (showDelete) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SGColors.error)
                        }
                    }
                    TextButton(onClick = onSave) {
                        Text(
                            text = "SAVE",
                            style = SGTextStyles.label,
                            color = SGColors.accentBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SGSpacing.xl))

            // Mood Selector
            Text(
                text = "How do you feel?",
                style = SGTextStyles.label,
                color = SGColors.textSecondary
            )
            Spacer(modifier = Modifier.height(SGSpacing.sm))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(SGSpacing.sm)) {
                items(moods) { m ->
                    SGChip(
                        text = m,
                        style = if (mood == m) SGChipStyle.Active else SGChipStyle.Inactive,
                        modifier = Modifier.clickable { 
                            onMoodChange(if (mood == m) null else m)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(SGSpacing.xl))

            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = {
                    Text(
                        text = "What's on your heart?",
                        style = SGTextStyles.heroTitle.copy(color = SGColors.textTertiary.copy(alpha = 0.3f), fontWeight = FontWeight.Normal),
                        lineHeight = 44.sp
                    )
                },
                textStyle = SGTextStyles.heroTitle.copy(color = SGColors.textPrimary, fontWeight = FontWeight.Normal),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = SGColors.accent
                )
            )
            
            SGPrimaryButton(
                text = "SAVE REFLECTION",
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
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
