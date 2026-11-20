package com.focusgate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusgate.app.data.datastore.ThemeMode
import com.focusgate.app.ui.navigation.FocusGateNavHost
import com.focusgate.app.ui.theme.FocusGateTheme

class MainActivity : ComponentActivity() {

    private val container: AppContainer get() = (application as FocusGateApplication).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by container.settingsDataStore.settingsFlow.collectAsStateWithLifecycle(
                initialValue = com.focusgate.app.data.datastore.AppSettings()
            )
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> systemDark
            }
            FocusGateTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FocusGateNavHost(container = container)
                }
            }
        }
    }
}
