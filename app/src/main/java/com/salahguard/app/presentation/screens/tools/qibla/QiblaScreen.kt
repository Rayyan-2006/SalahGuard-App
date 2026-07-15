package com.salahguard.app.presentation.screens.tools.qibla

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salahguard.app.presentation.components.HomeSanctuaryBackground
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGTextStyles
import com.salahguard.app.presentation.theme.NightBackground
import com.salahguard.app.R
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.ui.text.style.TextAlign
import com.salahguard.app.presentation.screens.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    onNavigateBack: () -> Unit,
    viewModel: QiblaViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val rotation by animateFloatAsState(
        targetValue = -uiState.azimuth,
        animationSpec = tween(durationMillis = 500)
    )

    val qiblaRotation by animateFloatAsState(
        targetValue = uiState.qiblaDirection - uiState.azimuth,
        animationSpec = tween(durationMillis = 500)
    )

    HomeSanctuaryBackground(currentPrayerName = homeUiState.currentPrayerName) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Qibla Direction",
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .rotate(rotation),
                    contentAlignment = Alignment.Center
                ) {
                    // Compass Background
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_qibla_compass),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = SGColors.textTertiary.copy(alpha = 0.4f)
                    )
                    
                    // Qibla Pointer
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(qiblaRotation),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_kaaba_pointer),
                            contentDescription = "Qibla",
                            tint = SGColors.accentBright,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = if (abs(uiState.azimuth - uiState.qiblaDirection) < 5) "Facing Kaaba" else "Align your phone",
                    style = SGTextStyles.cardTitle,
                    color = if (abs(uiState.azimuth - uiState.qiblaDirection) < 5) SGColors.success else SGColors.textPrimary
                )

                Text(
                    text = "${uiState.qiblaDirection.toInt()}°",
                    style = SGTextStyles.heroTitle.copy(fontSize = 48.sp),
                    color = SGColors.accent
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.distanceToKaaba > 0) {
                    Text(
                        text = "Distance to Kaaba",
                        style = SGTextStyles.caption,
                        color = SGColors.textSecondary
                    )
                    Text(
                        text = "${String.format("%.1f", uiState.distanceToKaaba)} km",
                        style = SGTextStyles.body.copy(fontWeight = FontWeight.Bold),
                        color = SGColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    color = SGColors.glassFillSubtle,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.CompassCalibration,
                            contentDescription = null,
                            tint = SGColors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Compass Calibration: Move your phone in a figure-eight motion if inaccurate.",
                            style = SGTextStyles.caption,
                            color = SGColors.textSecondary,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

private fun abs(value: Float): Float = if (value < 0) -value else value
