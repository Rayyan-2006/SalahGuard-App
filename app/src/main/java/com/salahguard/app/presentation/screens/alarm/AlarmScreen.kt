package com.salahguard.app.presentation.screens.alarm

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.presentation.components.BreathingBackground
import com.salahguard.app.presentation.theme.WarmIvory
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AlarmScreen(
    prayerName: PrayerName,
    onDismiss: () -> Unit,
    onSnooze: (Int) -> Unit,
    onMarkAsPrayed: () -> Unit
) {
    val currentTime = remember { mutableStateOf(LocalTime.now()) }
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = LocalTime.now()
            kotlinx.coroutines.delay(1000)
        }
    }

    BreathingBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = prayerName.name,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = currentTime.value.format(timeFormatter),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                color = WarmIvory,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Dismiss", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onMarkAsPrayed,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Mark as Prayed", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Snooze:", color = WarmIvory.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15).forEach { mins ->
                    OutlinedButton(
                        onClick = { onSnooze(mins) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvory.copy(alpha = 0.3f))
                    ) {
                        Text("${mins}m", color = WarmIvory, fontSize = 14.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = getEncouragingMessage(prayerName),
                style = MaterialTheme.typography.bodyLarge,
                color = WarmIvory.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun getEncouragingMessage(prayerName: PrayerName): String {
    return when (prayerName) {
        PrayerName.FAJR -> "May Allah make your Fajr easy and accepted."
        else -> "A peaceful connection awaits you."
    }
}
