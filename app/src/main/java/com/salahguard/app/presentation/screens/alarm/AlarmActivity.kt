package com.salahguard.app.presentation.screens.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.presentation.theme.SalahGuardTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {
    private val viewModel: AlarmViewModel by viewModels()
    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        turnScreenOnAndShowOverLockscreen()
        enableEdgeToEdge()
        
        val prayerNameString = intent.getStringExtra(EXTRA_PRAYER_NAME)
        val prayerName = prayerNameString?.let { PrayerName.valueOf(it) } ?: PrayerName.FAJR

        startAlarm()

        setContent {
            SalahGuardTheme {
                AlarmScreen(
                    prayerName = prayerName,
                    onDismiss = { stopAlarmAndFinish() },
                    onSnooze = { mins ->
                        viewModel.snooze(prayerName, mins)
                        stopAlarmAndFinish()
                    },
                    onMarkAsPrayed = {
                        viewModel.markAsPrayed(prayerName)
                        stopAlarmAndFinish()
                    }
                )
            }
        }
    }

    private fun startAlarm() {
        val soundMode = runBlocking<String> { viewModel.getAlarmSound().first() }
        
        if (soundMode != "SILENT") {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            ringtone?.play()
        }

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0))
        } else {
            vibrator.vibrate(longArrayOf(0, 1000, 1000), 0)
        }
    }

    private fun stopAlarmAndFinish() {
        ringtone?.stop()
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
        
        val prayerNameString = intent.getStringExtra(EXTRA_PRAYER_NAME)
        val prayerName = prayerNameString?.let { PrayerName.valueOf(it) }
        prayerName?.let {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(it.ordinal * 100)
        }

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
    }

    private fun turnScreenOnAndShowOverLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestDismissKeyguard(this@AlarmActivity, null)
            }
        }
    }

    companion object {
        private const val EXTRA_PRAYER_NAME = "extra_prayer_name"

        fun createIntent(context: Context, prayerName: PrayerName): Intent {
            return Intent(context, AlarmActivity::class.java).apply {
                putExtra(EXTRA_PRAYER_NAME, prayerName.name)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}
