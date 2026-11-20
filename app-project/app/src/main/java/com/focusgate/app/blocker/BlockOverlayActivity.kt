package com.focusgate.app.blocker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusgate.app.FocusGateApplication
import com.focusgate.app.data.datastore.AppSettings
import com.focusgate.app.data.datastore.ThemeMode
import com.focusgate.app.ui.challenge.ChallengeScreen
import com.focusgate.app.ui.challenge.ChallengeViewModel
import com.focusgate.app.ui.theme.FocusGateTheme

/** The forced, full-screen challenge gate launched on top of Instagram by
 *  [InstagramAccessibilityService]. Finishing it (only reachable by completing the
 *  challenge) hands control back to Instagram. */
class BlockOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as FocusGateApplication).container

        setContent {
            val settings by container.settingsDataStore.settingsFlow.collectAsStateWithLifecycle(
                initialValue = AppSettings()
            )
            val darkTheme = settings.themeMode != ThemeMode.LIGHT

            val viewModel: ChallengeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = ChallengeViewModel.Factory(
                    container.questionRepository,
                    container.statsRepository,
                    container.settingsDataStore,
                    triggeredByInstagram = true
                )
            )

            FocusGateTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChallengeScreen(
                        viewModel = viewModel,
                        primaryActionLabel = "Entrar a Instagram",
                        onPrimaryAction = { returnToInstagram() }
                    )
                }
            }
        }
    }

    private fun returnToInstagram() {
        val launchIntent = packageManager.getLaunchIntentForPackage(InstagramAccessibilityService.INSTAGRAM_PACKAGE)
        if (launchIntent != null) {
            startActivity(launchIntent)
        }
        finish()
    }
}
