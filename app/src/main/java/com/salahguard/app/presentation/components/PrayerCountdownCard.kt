package com.salahguard.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahguard.app.presentation.theme.*

@Composable
fun PrayerCountdownCard(
    prayerName: String,
    remainingSeconds: Long,
    modifier: Modifier = Modifier,
    currentPrayerName: String? = null,
    currentPrayerTime: String = "",
    nextPrayerTime: String = ""
) {
    val transition = rememberInfiniteTransition(label = "heroGlow")
    val ambientGlow by transition.animateFloat(
        initialValue = 0.08f, targetValue = 0.18f,
        animationSpec = infiniteRepeatable(tween(5000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                shadowElevation = 16f
                shape = RoundedCornerShape(24.dp)
            }
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        EmeraldMid.copy(alpha = 0.45f), // Matching the TodayJourneyCard transparency
                        EmeraldDeep.copy(alpha = 0.35f)
                    )
                )
            )
            .border(
                BorderStroke(0.8.dp, Color.White.copy(alpha = 0.15f)),
                RoundedCornerShape(24.dp)
            )
    ) {
        // Subtle top-right atmospheric glow as seen in the picture
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldBright.copy(alpha = ambientGlow), Color.Transparent),
                        center = Offset(900f, 150f),
                        radius = 700f
                    )
                )
        )

        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HEADER SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mosque-style icon container
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                            .background(Color.White.copy(0.04f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalance, // Closest to the dome shape
                            contentDescription = null,
                            tint = GoldBright.copy(0.9f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Current Prayer",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SageMist.copy(0.6f),
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = currentPrayerName ?: "Checking...",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarmIvory
                            )
                        )
                        Text(
                            text = currentPrayerTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SageMist.copy(0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // SalahGuard Badge
                Surface(
                    color = Color.White.copy(0.06f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, GoldBright.copy(0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SalahGuard",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = GoldBright,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TIME REMAINING DIVIDER
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Box(modifier = Modifier.weight(1f).height(0.5.dp).background(Color.White.copy(0.12f)))
                Text(
                    text = "  TIME REMAINING  ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SageMist.copy(0.5f),
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Box(modifier = Modifier.weight(1f).height(0.5.dp).background(Color.White.copy(0.12f)))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // HERO COUNTDOWN DIGITS
            val h = remainingSeconds / 3600
            val m = (remainingSeconds % 3600) / 60
            val s = remainingSeconds % 60
            
            Text(
                text = "%02d:%02d:%02d".format(h, m, s),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 58.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmIvory,
                    letterSpacing = (-0.5).sp
                )
            )

            // SUB-LABELS: HRS MIN SEC
            Row(
                modifier = Modifier.width(240.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("HRS", "MIN", "SEC").forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SageMist.copy(0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // NEXT PRAYER INSET BOX
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(0.04f)) // Replaced Black for better transparency harmony
                    .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Next Prayer",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SageMist.copy(0.5f)
                            )
                        )
                        Text(
                            text = prayerName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarmIvory
                            )
                        )
                        Text(
                            text = nextPrayerTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SageMist.copy(0.7f)
                            )
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = null,
                        tint = GoldBright.copy(0.35f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // BOTTOM PILL
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Spa,
                    contentDescription = null,
                    tint = GoldBright.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Stay consistent, stay blessed.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SageMist.copy(0.7f),
                        fontStyle = FontStyle.Italic
                    )
                )
            }
        }
    }
}
