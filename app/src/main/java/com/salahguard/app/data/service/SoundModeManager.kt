package com.salahguard.app.data.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.os.Build
import com.salahguard.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundModeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun hasDndPermission(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun canWriteSettings(): Boolean {
        return Settings.System.canWrite(context)
    }

    suspend fun applySalahGuardMode() {
        val mode = userPreferencesRepository.getSalahGuardMode().first()

        if (mode == "DISABLED") return

        // Handle Brightness Dimming
        if (userPreferencesRepository.isBrightnessDimEnabled().first() && canWriteSettings()) {
            val currentBrightness = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            userPreferencesRepository.setOriginalBrightness(currentBrightness)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 20) // Dim to low
        }

        // Handle Screen Timeout
        if (userPreferencesRepository.isScreenTimeoutEnabled().first() && canWriteSettings()) {
            val currentTimeout = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
            userPreferencesRepository.setOriginalScreenTimeout(currentTimeout)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 15000) // 15 seconds
        }

        // Save current state if not already saved
        val currentRingerMode = audioManager.ringerMode
        userPreferencesRepository.setOriginalRingerMode(currentRingerMode)

        when (mode) {
            "SILENT" -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            "VIBRATE" -> audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            "DND" -> {
                if (hasDndPermission()) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                } else {
                    // Fallback to silent if no DND permission
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                }
            }
        }
    }

    suspend fun restoreOriginalMode() {
        // Restore Brightness
        if (canWriteSettings()) {
            userPreferencesRepository.getOriginalBrightness().first()?.let {
                Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, it)
                userPreferencesRepository.setOriginalBrightness(null)
            }
            
            // Restore Timeout
            userPreferencesRepository.getOriginalScreenTimeout().first()?.let {
                Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, it)
                userPreferencesRepository.setOriginalScreenTimeout(null)
            }
        }

        val originalMode = userPreferencesRepository.getOriginalRingerMode().first() ?: return
        
        val currentMode = userPreferencesRepository.getSalahGuardMode().first()
        if (currentMode == "DND" && hasDndPermission()) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
        
        audioManager.ringerMode = originalMode
        userPreferencesRepository.setOriginalRingerMode(null)
    }
}
