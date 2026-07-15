package com.salahguard.app.presentation.screens.protection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahguard.app.presentation.components.BreathingBackground
import com.salahguard.app.presentation.designsystem.SGColors
import com.salahguard.app.presentation.designsystem.SGShapes
import com.salahguard.app.presentation.designsystem.SGSpacing
import com.salahguard.app.presentation.designsystem.SGTextStyles
import com.salahguard.app.presentation.theme.GoldBright
import com.salahguard.app.presentation.theme.NightBackground
import com.salahguard.app.presentation.theme.WarmIvory

class AppBlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppBlockScreen(onBackToPrayer = { finish() })
        }
    }
}

@Composable
fun AppBlockScreen(onBackToPrayer: () -> Unit) {
    BreathingBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Shield / Block Icon with Glow
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(GoldBright.copy(alpha = 0.1f), CircleShape)
                    )
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = GoldBright,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(SGSpacing.xl))

                Text(
                    text = "Stay in the Presence",
                    style = SGTextStyles.cardTitle.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    color = WarmIvory,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(SGSpacing.md))

                Text(
                    text = "SalahGuard is helping you protect this sacred moment. Focus on your prayer and reconnection with Allah.",
                    style = SGTextStyles.body.copy(lineHeight = 24.sp),
                    color = SGColors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onBackToPrayer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldBright),
                    shape = SGShapes.large
                ) {
                    Text(
                        "Back to Prayer",
                        style = SGTextStyles.body.copy(color = NightBackground, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
