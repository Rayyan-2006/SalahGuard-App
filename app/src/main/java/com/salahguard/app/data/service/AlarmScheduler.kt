package com.salahguard.app.data.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.salahguard.app.domain.model.Prayer
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun getPowerManager() = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
    fun getPackageName() = context.packageName

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_REMINDER_TYPE = "extra_reminder_type"
        
        const val TYPE_BEFORE = 0
        const val TYPE_AT = 1
        const val TYPE_AFTER = 2
        const val TYPE_WINDOW_END = 3
        const val TYPE_FAJR_EXTRA = 4
    }

    fun schedulePrayerReminders(prayer: Prayer) {
        if (prayer.name == PrayerName.SUNRISE) return

        cancelReminders(prayer.name)

        scope.launch {
            val offset = userPreferencesRepository.getReminderTimeOffset().first()
            
            // 1. Dynamic reminder before (5, 10, 15, 30 mins)
            if (offset > 0) {
                scheduleAlarm(prayer, TYPE_BEFORE, prayer.scheduledTime.minusMinutes(offset.toLong()))
            }
            
            // 2. At prayer time
            scheduleAlarm(prayer, TYPE_AT, prayer.scheduledTime)
            
            // 3. 15 minutes after (remind if pending)
            scheduleAlarm(prayer, TYPE_AFTER, prayer.scheduledTime.plusMinutes(15))

            // 4. SalahGuard Window End (20 minutes after start)
            scheduleAlarm(prayer, TYPE_WINDOW_END, prayer.scheduledTime.plusMinutes(20))

            // 5. Fajr Extra Reminder (15 mins before)
            if (prayer.name == PrayerName.FAJR && userPreferencesRepository.isExtraFajrReminderEnabled().first()) {
                scheduleAlarm(prayer, TYPE_FAJR_EXTRA, prayer.scheduledTime.minusMinutes(15))
            }
        }
    }

    private fun scheduleAlarm(prayer: Prayer, type: Int, time: LocalTime) {
        val now = LocalDateTime.now()
        var alarmDateTime = LocalDateTime.of(prayer.date, time)
        
        // Handle rollover to next day (e.g., Isha 15-min reminder after midnight)
        if (type != TYPE_BEFORE && time.isBefore(prayer.scheduledTime)) {
            alarmDateTime = alarmDateTime.plusDays(1)
        }
        
        if (alarmDateTime.isBefore(now)) return

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(EXTRA_PRAYER_NAME, prayer.name.name)
            putExtra(EXTRA_REMINDER_TYPE, type)
        }

        val requestCode = getRequestCode(prayer.name, type)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (type == TYPE_AT || type == TYPE_FAJR_EXTRA) {
            val showIntent = Intent(context, com.salahguard.app.presentation.MainActivity::class.java)
            val showPendingIntent = PendingIntent.getActivity(
                context,
                requestCode + 1000,
                showIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
            try {
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } catch (e: SecurityException) {
                // Fallback if permission is still missing somehow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    fun cancelReminders(prayerName: PrayerName) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        listOf(TYPE_BEFORE, TYPE_AT, TYPE_AFTER, TYPE_WINDOW_END, TYPE_FAJR_EXTRA).forEach { type ->
            val requestCode = getRequestCode(prayerName, type)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }

    private fun getRequestCode(prayerName: PrayerName, type: Int): Int {
        return prayerName.ordinal * 10 + type
    }

    fun snoozeAlarm(prayerName: PrayerName, minutes: Int) {
        val snoozeTime = LocalTime.now().plusMinutes(minutes.toLong())
        // For simplicity, using a dummy Prayer object for the snooze logic
        val dummyPrayer = Prayer(
            name = prayerName,
            date = LocalDate.now(),
            scheduledTime = snoozeTime, // Not the real scheduled time, but the alarm time
            status = com.salahguard.app.domain.model.PrayerStatus.PENDING
        )
        scheduleAlarm(dummyPrayer, TYPE_AT, snoozeTime)
    }
}
