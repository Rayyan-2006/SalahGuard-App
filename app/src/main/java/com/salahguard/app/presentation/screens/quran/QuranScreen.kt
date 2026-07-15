package com.salahguard.app.presentation.screens.quran

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
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
import com.salahguard.app.domain.model.Ayah
import com.salahguard.app.domain.model.Surah
import com.salahguard.app.domain.service.PlayerState
import com.salahguard.app.presentation.components.*
import com.salahguard.app.presentation.designsystem.*
import com.salahguard.app.presentation.designsystem.components.*
import com.salahguard.app.presentation.theme.*
import com.salahguard.app.presentation.screens.home.HomeViewModel

@Composable
fun QuranScreen(
    viewModel: QuranViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToPrayers: () -> Unit,
    onNavigateToJourney: () -> Unit,
    onNavigateToReflection: (String?) -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToMosques: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(BottomNavDestination.LEARN) }
    val haptic = LocalHapticFeedback.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    // sync background with current prayer atmosphere
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
                    if (!uiState.isReadingMode) {
                        scaleX = swipeScale
                        scaleY = swipeScale
                        translationX = totalDrag * 0.4f
                        alpha = (1f - (kotlin.math.abs(totalDrag) / 1000f)).coerceIn(0.7f, 1f)
                    }
                }
                .pointerInput(uiState.isReadingMode) {
                    if (!uiState.isReadingMode) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                            },
                            onDragEnd = {
                                if (totalDrag > 140) { // Swipe Right
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onNavigateToPrayers()
                                } else if (totalDrag < -140) { // Swipe Left
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
                if (!uiState.isReadingMode) {
                    SalahGuardBottomNavBar(
                        selected = selectedTab,
                        onSelect = {
                            selectedTab = it
                            when (it) {
                                BottomNavDestination.HOME -> onNavigateToHome()
                                BottomNavDestination.PRAYERS -> onNavigateToPrayers()
                                BottomNavDestination.JOURNEY -> onNavigateToJourney()
                                BottomNavDestination.REFLECTION -> onNavigateToReflection(null)
                                else -> {}
                            }
                        }
                    )
                }
            }
        ) { padding ->
            AnimatedContent(
                targetState = uiState.isReadingMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "quranModeTransition"
            ) { isReadingMode ->
                if (isReadingMode) {
                    ReadingView(
                        uiState = uiState,
                        onBack = { viewModel.exitReadingMode() },
                        onPlayAyah = { viewModel.playAyah(it) },
                        onPlaySurah = { viewModel.playSurah() },
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSeek = { viewModel.seekTo(it) },
                        onSetSpeed = { viewModel.setPlaybackSpeed(it) },
                        onNext = { viewModel.nextAyah() },
                        onPrevious = { viewModel.previousAyah() },
                        padding = padding
                    )
                } else {
                    LearnMainView(
                        uiState = uiState,
                        visible = visible,
                        onSurahClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.selectSurah(it) 
                        },
                        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                        padding = padding
                    )
                }
            }
        }
    }
}

@Composable
private fun LearnMainView(
    uiState: QuranUiState,
    visible: Boolean,
    onSurahClick: (Surah) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    padding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(horizontal = SGSpacing.lg, vertical = SGSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(SGSpacing.xl)
    ) {
        // 1. Page Title
        item {
            StaggeredEntrance(visible = visible, index = 0) {
                Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.xs)) {
                    Text(
                        text = "Learn",
                        style = SGTextStyles.heroTitle,
                        color = SGColors.textPrimary
                    )
                    Text(
                        text = "Grow a little every day.",
                        style = SGTextStyles.body.copy(
                            color = SGColors.textSecondary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // 2. Today's Wisdom Hero Card
        item {
            StaggeredEntrance(visible = visible, index = 1) {
                LearnHeroCard(
                    surahs = uiState.surahs,
                    onSurahClick = onSurahClick
                )
            }
        }

        // 3. Continue Learning
        if (uiState.lastOpenedSurah != null) {
            item {
                StaggeredEntrance(visible = visible, index = 2) {
                    ContinueLearningSection(
                        surah = uiState.lastOpenedSurah,
                        onClick = { onSurahClick(uiState.lastOpenedSurah) }
                    )
                }
            }
        } else {
            item {
                StaggeredEntrance(visible = visible, index = 2) {
                    StartLearningCard()
                }
            }
        }

        // 5. Learning Library (Surah list)
        item {
            StaggeredEntrance(visible = visible, index = 3) {
                SGSectionTitle(
                    title = "Learning Library",
                    subtitle = "Deepen your understanding"
                )
            }
        }

        // Search Bar within Library
        item {
            StaggeredEntrance(visible = visible, index = 4) {
                LearnSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange
                )
            }
        }

        if (uiState.isLoading && uiState.surahs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SGColors.accent)
                }
            }
        } else if (uiState.filteredSurahs.isEmpty()) {
            item { EmptySearchState() }
        } else {
            items(uiState.filteredSurahs) { surah ->
                StaggeredEntrance(visible = visible, index = 5) {
                    LearningLibraryCard(surah = surah, onClick = { onSurahClick(surah) })
                }
            }
        }

        // 6. Daily Reflection Quote
        item {
            StaggeredEntrance(visible = visible, index = 6) {
                DailyReflectionQuoteCard()
            }
        }

        item { Spacer(modifier = Modifier.height(SGSpacing.xl)) }
    }
}

@Composable
private fun LearnHeroCard(surahs: List<Surah>, onSurahClick: (Surah) -> Unit) {
    val ashSharh = remember(surahs) { surahs.find { it.id == 94 } }
    
    SGHeroCard {
        Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S WISDOM",
                    style = SGTextStyles.label,
                    color = SGColors.accent,
                )
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = SGColors.accentBright.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = "“Indeed, with hardship [will be] ease.”",
                style = SGTextStyles.heroTitle.copy(fontSize = 26.sp, lineHeight = 34.sp),
                color = SGColors.textPrimary
            )
            
            Text(
                text = "Surah Ash-Sharh 94:6",
                style = SGTextStyles.body,
                color = SGColors.textSecondary
            )
            
            SGPrimaryButton(
                text = "Read Full Surah",
                onClick = { ashSharh?.let { onSurahClick(it) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ContinueLearningSection(surah: Surah, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SGSpacing.md)) {
        SGSectionTitle(title = "Continue Learning")
        SGGlassCard(onClick = onClick) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(SGShapes.medium)
                        .background(SGColors.accent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        tint = SGColors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(SGSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = surah.englishName,
                        style = SGTextStyles.cardTitle,
                        color = SGColors.textPrimary
                    )
                    Text(
                        text = "Surah ${surah.id} • ${surah.verseCount} Ayahs",
                        style = SGTextStyles.caption,
                        color = SGColors.textSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = SGColors.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StartLearningCard() {
    SGGlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(SGShapes.medium)
                    .background(SGColors.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoStories,
                    contentDescription = null,
                    tint = SGColors.accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(SGSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Start Learning",
                    style = SGTextStyles.cardTitle,
                    color = SGColors.textPrimary
                )
                Text(
                    text = "Begin your journey today",
                    style = SGTextStyles.caption,
                    color = SGColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun LearnSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .then(SGGlass.subtle()),
        placeholder = { Text("Search Library...", style = SGTextStyles.body, color = SGColors.textTertiary) },
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
private fun LearningLibraryCard(surah: Surah, onClick: () -> Unit) {
    SGGlassCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Surah Number
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(SGShapes.medium)
                    .background(SGColors.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = surah.id.toString(),
                    style = SGTextStyles.numeral.copy(fontSize = 18.sp, color = SGColors.accent),
                )
            }
            
            Spacer(modifier = Modifier.width(SGSpacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = surah.englishName,
                        style = SGTextStyles.cardTitle,
                        color = SGColors.textPrimary
                    )
                    Text(
                        text = surah.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = ArabicFontFamily,
                            color = SGColors.accent.copy(alpha = 0.8f)
                        )
                    )
                }
                Text(
                    text = "Quran • ${surah.verseCount / 2} min read",
                    style = SGTextStyles.caption,
                    color = SGColors.textSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun DailyReflectionQuoteCard() {
    SGGlassCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SGSpacing.sm)
        ) {
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                tint = SGColors.accent.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "“The best of you are those who learn the Quran and teach it.”",
                style = SGTextStyles.body,
                color = SGColors.textPrimary,
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Text(
                text = "Prophet Muhammad (ﷺ)",
                style = SGTextStyles.caption,
                color = SGColors.textSecondary
            )
        }
    }
}

@Composable
private fun ReadingView(
    uiState: QuranUiState,
    onBack: () -> Unit,
    onPlayAyah: (Ayah) -> Unit,
    onPlaySurah: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    padding: PaddingValues
) {
    val surah = uiState.selectedSurah
    val verses = uiState.verses

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // Custom reading mode header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SGSpacing.md)
                .then(SGGlass.standard()),
            color = Color.Transparent,
            shape = SGShapes.large
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SGSpacing.md, vertical = SGSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SGColors.textPrimary)
                }
                Spacer(modifier = Modifier.width(SGSpacing.sm))
                Column {
                    Text(
                        text = surah?.englishName ?: "",
                        style = SGTextStyles.cardTitle,
                        color = SGColors.textPrimary
                    )
                    Text(
                        text = "${surah?.verseCount} Ayahs • ${surah?.revelationType}",
                        style = SGTextStyles.caption,
                        color = SGColors.accent
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onPlaySurah) {
                    Icon(Icons.Default.PlayCircle, contentDescription = "Play Surah", tint = SGColors.accent, modifier = Modifier.size(32.dp))
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (verses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SGColors.accent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = SGSpacing.lg, vertical = SGSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(SGSpacing.xl)
                ) {
                    // Bismillah card for most Surahs
                    if (surah?.id != 1 && surah?.id != 9) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = SGSpacing.lg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontFamily = ArabicFontFamily,
                                        color = SGColors.accent,
                                        fontSize = 28.sp
                                    )
                                )
                            }
                        }
                    }
                    
                    itemsIndexed(verses) { index, ayah ->
                        AyahItem(
                            ayah = ayah,
                            isPlaying = uiState.playingAyahId == ayah.number,
                            onPlayClick = { onPlayAyah(ayah) }
                        )
                    }
                    // generous spacer to prevent clash with audio player
                    item { Spacer(modifier = Modifier.height(160.dp)) }
                }
            }

            // gradient scrim to help prevent audio player clash
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, NightBackground.copy(alpha = 0.85f)),
                        )
                    )
            )

            // Compact Audio Player
            if (uiState.playerState.isPlaying || uiState.playerState.currentPosition > 0 || uiState.playerState.isBuffering) {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    CompactAudioPlayer(
                        playerState = uiState.playerState,
                        onTogglePlayPause = onTogglePlayPause,
                        onSeek = onSeek,
                        onSetSpeed = onSetSpeed,
                        onNext = onNext,
                        onPrevious = onPrevious
                    )
                }
            }
        }
    }
}

@Composable
private fun AyahItem(
    ayah: Ayah,
    isPlaying: Boolean = false,
    onPlayClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SGSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SGColors.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ayah.number.toString(),
                    style = SGTextStyles.label.copy(
                        color = SGColors.accent,
                        fontSize = 12.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(SGSpacing.md))
            IconButton(onClick = onPlayClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                    contentDescription = "Play Ayah",
                    tint = if (isPlaying) SGColors.accent else SGColors.textTertiary
                )
            }
            Spacer(modifier = Modifier.width(SGSpacing.md))
            SGDivider(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(SGSpacing.lg))
        
        Text(
            text = ayah.text,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = ArabicFontFamily,
                lineHeight = 60.sp,
                fontSize = 34.sp,
                color = SGColors.textPrimary
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
        
        Spacer(modifier = Modifier.height(SGSpacing.lg))
        
        Text(
            text = ayah.translation,
            style = SGTextStyles.body.copy(
                lineHeight = 28.sp,
                color = SGColors.textSecondary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun CompactAudioPlayer(
    playerState: PlayerState,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    SGGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SGSpacing.lg)
    ) {
        Column(modifier = Modifier.padding(SGSpacing.xs)) {
            if (playerState.isBuffering) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp), color = SGColors.accent)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { 
                    val nextSpeed = when(playerState.playbackSpeed) {
                        0.75f -> 1.0f
                        1.0f -> 1.25f
                        1.25f -> 1.5f
                        else -> 0.75f
                    }
                    onSetSpeed(nextSpeed)
                }) {
                    Text(text = "${playerState.playbackSpeed}x", style = SGTextStyles.label, color = SGColors.accent)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = SGColors.textPrimary)
                    }
                    FloatingActionButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.size(48.dp),
                        containerColor = SGColors.accent,
                        contentColor = NightBackground,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = SGColors.textPrimary)
                    }
                }

                if (playerState.error != null) {
                    Icon(Icons.Default.Error, contentDescription = "Error", tint = SGColors.error)
                } else {
                    Text(
                        text = formatTime(playerState.currentPosition),
                        style = SGTextStyles.caption,
                        color = SGColors.textSecondary
                    )
                }
            }

            Slider(
                value = playerState.currentPosition.toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..playerState.duration.toFloat().coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = SGColors.accentBright,
                    activeTrackColor = SGColors.accent,
                    inactiveTrackColor = SGColors.glassBorder
                )
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = SGSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = SGColors.textTertiary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(SGSpacing.md))
        Text(
            text = "No results found",
            style = SGTextStyles.cardTitle,
            color = SGColors.textPrimary
        )
        Text(
            text = "Try searching for another topic",
            style = SGTextStyles.body,
            color = SGColors.textSecondary
        )
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
