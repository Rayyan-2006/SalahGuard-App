package com.salahguard.app.presentation.screens.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salahguard.app.domain.model.Achievement
import com.salahguard.app.domain.model.AchievementCategory
import com.salahguard.app.presentation.components.BottomNavDestination
import com.salahguard.app.presentation.components.BreathingBackground
import com.salahguard.app.presentation.components.SalahGuardBottomNavBar
import com.salahguard.app.presentation.components.SalahGuardCard
import com.salahguard.app.presentation.theme.SageMist
import com.salahguard.app.presentation.theme.WarmIvory

@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToPrayers: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToJourney: () -> Unit,
    onNavigateToReflection: (String?) -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToMosques: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BreathingBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                SalahGuardBottomNavBar(
                    selected = BottomNavDestination.JOURNEY, // Achievements linked to Journey
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
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.headlineLarge,
                    color = WarmIvory
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${uiState.unlockedCount} of ${uiState.totalCount} milestones reached",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SageMist
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        AchievementCategory.entries.forEach { category ->
                            val categoryAchievements = uiState.achievements.filter { it.category == category }
                            if (categoryAchievements.isNotEmpty()) {
                                item {
                                    Text(
                                        text = category.name.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SageMist,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                items(categoryAchievements) { achievement ->
                                    AchievementItem(achievement = achievement)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.newlyUnlocked?.let { achievement ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissUnlockedDialog() },
            title = { Text("🎉 Achievement Unlocked!") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = achievement.icon, fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = achievement.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = achievement.description, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissUnlockedDialog() }) {
                    Text("Alhamdulillah")
                }
            }
        )
    }
}

@Composable
private fun AchievementItem(achievement: Achievement) {
    SalahGuardCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (achievement.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = achievement.icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (achievement.isUnlocked) WarmIvory else SageMist,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SageMist
                )
                if (!achievement.isUnlocked && achievement.targetValue > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { achievement.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}
