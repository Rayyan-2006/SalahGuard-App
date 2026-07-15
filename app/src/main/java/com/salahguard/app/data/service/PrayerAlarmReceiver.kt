package com.salahguard.app.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.domain.model.PrayerStatus
import com.salahguard.app.domain.repository.PrayerRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class PrayerAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var prayerRepository: PrayerRepository

    @Inject
    lateinit var soundModeManager: SoundModeManager

    @Inject
    lateinit var userPreferencesRepository: com.salahguard.app.domain.repository.UserPreferencesRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val prayerNameString = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: return
        val type = intent.getIntExtra(AlarmScheduler.EXTRA_REMINDER_TYPE, -1)
        val prayerName = PrayerName.valueOf(prayerNameString)

        Log.d("PrayerAlarmReceiver", "Received alarm for $prayerNameString, type: $type")

        val pendingResult = goAsync()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "SalahGuard:AlarmWakeLock")
        wakeLock.acquire(10 * 1000L /*10 seconds*/)

        scope.launch {
            try {
                if (type == AlarmScheduler.TYPE_WINDOW_END) {
                    soundModeManager.restoreOriginalMode()
                    return@launch
                }

                val todayPrayers = prayerRepository.getPrayersForDate(LocalDate.now()).first()
                val prayer = todayPrayers.find { it.name == prayerName } ?: return@launch

                if (type == AlarmScheduler.TYPE_AT) {
                    soundModeManager.applySalahGuardMode()
                }

                // Check if alarm is enabled for this prayer
                val isAlarmEnabled = userPreferencesRepository.isPrayerAlarmEnabled(prayerName.name).first()
                Log.d("PrayerAlarmReceiver", "Alarm enabled for $prayerName: $isAlarmEnabled")
                
                if (isAlarmEnabled && (type == AlarmScheduler.TYPE_AT || type == AlarmScheduler.TYPE_FAJR_EXTRA)) {
                    Log.d("PrayerAlarmReceiver", "Triggering full screen alarm activity for $prayerName")
                    val alarmIntent = com.salahguard.app.presentation.screens.alarm.AlarmActivity.createIntent(context, prayerName)
                    val pendingIntent = android.app.PendingIntent.getActivity(
                        context,
                        prayerName.ordinal,
                        alarmIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    
                    notificationHelper.showAlarmNotification(
                        title = "SalahGuard Alarm",
                        message = "It's time for ${prayerName.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        notificationId = prayerName.ordinal * 100,
                        fullScreenIntent = pendingIntent
                    )
                    return@launch
                }

                // Check master toggle for notifications
                if (!userPreferencesRepository.isNotificationsEnabled().first()) return@launch

                // Check per-prayer toggle
                if (!userPreferencesRepository.isPrayerNotificationEnabled(prayerName.name).first()) return@launch

                if (prayer.status == PrayerStatus.COMPLETED) return@launch

                if (type == AlarmScheduler.TYPE_AFTER && prayer.status != PrayerStatus.PENDING) {
                    return@launch
                }

                val title = "SalahGuard"
                val message = getMessage(prayerName, type)
                val notificationId = prayerName.ordinal * 10 + type

                notificationHelper.showPrayerNotification(title, message, notificationId)
            } finally {
                if (wakeLock.isHeld) wakeLock.release()
                pendingResult.finish()
            }
        }
    }

    private fun getMessage(prayerName: PrayerName, type: Int): String {
        return when (type) {
            AlarmScheduler.TYPE_BEFORE -> when (prayerName) {
                PrayerName.FAJR -> "🌅 Fajr begins soon. May your day start with remembrance of Allah."
                PrayerName.DHUHR -> "🕌 Dhuhr is approaching. Take a peaceful moment for Salah."
                PrayerName.ASR -> "🌤️ Asr is near. Pause and reconnect."
                PrayerName.MAGHRIB -> "🌇 Maghrib has arrived soon. End your day with gratitude."
                PrayerName.ISHA -> "🌙 Isha is coming. Rest your heart in prayer."
                else -> "A peaceful moment for prayer is approaching."
            }
            AlarmScheduler.TYPE_AT -> when (prayerName) {
                PrayerName.FAJR -> "🌅 It's time for Fajr. Start your day with light."
                PrayerName.DHUHR -> "🕌 Dhuhr time. A mid-day pause for peace."
                PrayerName.ASR -> "🌤️ Asr is here. Reconnect and refresh."
                PrayerName.MAGHRIB -> "🌇 Maghrib has arrived. Gratitude for another day."
                PrayerName.ISHA -> "🌙 Isha time. Tranquility in the night."
                else -> "It's time for prayer. A moment of grace."
            }
            AlarmScheduler.TYPE_AFTER -> "A gentle reminder for your prayer. It's never too late to return."
            else -> "A reminder for your prayer."
        }
    }
}
