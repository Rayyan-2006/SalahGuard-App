package com.salahguard.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.salahguard.app.presentation.navigation.SalahGuardNavHost
import com.salahguard.app.presentation.theme.SalahGuardTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-Activity architecture: this is the only Android Activity in the app.
 * All screens are Composables managed by SalahGuardNavHost.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalahGuardTheme {
                SalahGuardNavHost()
            }
        }
    }
}
