package com.archy.vbr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.archy.vbr.data.PreferencesRepository
import com.archy.vbr.ui.screens.DashboardScreen
import com.archy.vbr.ui.screens.OverlaySettingsScreen
import com.archy.vbr.ui.theme.VBRTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesRepository = PreferencesRepository(applicationContext)

        enableEdgeToEdge()
        setContent {
            VBRTheme {
                var currentScreen by remember { mutableStateOf("dashboard") }

                when (currentScreen) {
                    "dashboard" -> {
                        DashboardScreen(
                            preferencesRepository = preferencesRepository,
                            onOpenOverlaySettings = {
                                currentScreen = "settings"
                            }
                        )
                    }
                    "settings" -> {
                        OverlaySettingsScreen(
                            preferencesRepository = preferencesRepository,
                            onBack = {
                                currentScreen = "dashboard"
                            }
                        )
                    }
                }
            }
        }
    }
}
