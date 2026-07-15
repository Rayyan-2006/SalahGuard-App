package com.salahguard.app.data.service

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.salahguard.app.R
import com.salahguard.app.domain.repository.UserPreferencesRepository
import com.salahguard.app.presentation.screens.protection.AppBlockActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class AppBlockingService : Service() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var isMonitoring = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SalahGuard Protection Active")
            .setContentText("Your prayer focus is being protected.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock) // Replace with app icon later
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        
        startMonitoring()
        
        return START_STICKY
    }

    private fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        serviceScope.launch {
            while (isActive) {
                if (userPreferencesRepository.isFocusModeEnabled().first()) {
                    val foregroundApp = getForegroundApp()
                    if (isAppBlocked(foregroundApp)) {
                        blockApp()
                    }
                }
                delay(1000) // Check every second
            }
        }
    }

    private fun getForegroundApp(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
        
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }

    private fun isAppBlocked(packageName: String?): Boolean {
        if (packageName == null) return false
        
        // List of apps that should NOT be blocked
        val allowedApps = listOf(
            packageName, // Our own app
            "com.android.settings",
            "com.android.phone",
            "com.android.server.telecom",
            "com.google.android.packageinstaller",
            "com.android.systemui"
        )
        
        if (packageName == this.packageName) return false
        
        // For now, block everything else if App Blocking is on.
        // We can make this list more specific later.
        return !allowedApps.contains(packageName)
    }

    private fun blockApp() {
        val intent = Intent(this, AppBlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SalahGuard Protection",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        isMonitoring = false
    }

    companion object {
        private const val CHANNEL_ID = "protection_service_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
