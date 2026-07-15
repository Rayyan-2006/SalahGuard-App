package com.salahguard.app.presentation.screens.tools.mosques

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salahguard.app.domain.model.Mosque
import com.salahguard.app.presentation.components.HomeSanctuaryBackground
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGSpacing
import com.salahguard.app.presentation.designsystem.SGTextStyles
import com.salahguard.app.presentation.designsystem.components.SGGlassCard
import com.salahguard.app.presentation.designsystem.components.SGPrimaryButton
import com.salahguard.app.presentation.designsystem.components.SGSectionTitle
import com.salahguard.app.presentation.screens.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosqueFinderScreen(
    onNavigateBack: () -> Unit,
    viewModel: MosqueFinderViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    HomeSanctuaryBackground(currentPrayerName = homeUiState.currentPrayerName) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Nearby Mosques",
                            style = SGTextStyles.sectionTitle,
                            color = SGColors.textPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SGColors.textPrimary
                            )
                        }
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
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = SGSpacing.lg, end = SGSpacing.lg, bottom = SGSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(SGSpacing.md)
                ) {
                    item {
                        SGSectionTitle(title = "Available Mosques")
                    }

                    if (uiState.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = SGColors.accent)
                            }
                        }
                    } else {
                        items(uiState.mosques) { mosque ->
                            MosqueCard(
                                mosque = mosque,
                                onNavigate = {
                                    val gmmIntentUri = Uri.parse("google.navigation:q=${mosque.latitude},${mosque.longitude}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MosqueCard(
    mosque: Mosque,
    onNavigate: () -> Unit
) {
    SGGlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SGSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mosque.name,
                    style = SGTextStyles.cardTitle,
                    color = SGColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${mosque.distance} km",
                        style = SGTextStyles.caption,
                        color = SGColors.accent
                    )
                    Text(
                        text = " • ",
                        color = SGColors.textTertiary
                    )
                    Text(
                        text = "${mosque.travelTimeMinutes} min drive",
                        style = SGTextStyles.caption,
                        color = SGColors.textSecondary
                    )
                }
            }

            IconButton(
                onClick = onNavigate,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = SGColors.accent.copy(alpha = 0.1f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Directions,
                    contentDescription = "Navigate",
                    tint = SGColors.accentBright
                )
            }
        }
    }
}
