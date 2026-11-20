package com.focusgate.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.focusgate.app.AppContainer
import com.focusgate.app.ui.challenge.ChallengeScreen
import com.focusgate.app.ui.challenge.ChallengeViewModel
import com.focusgate.app.ui.home.HomeScreen
import com.focusgate.app.ui.home.HomeViewModel
import com.focusgate.app.ui.onboarding.OnboardingScreen
import com.focusgate.app.ui.settings.SettingsScreen
import com.focusgate.app.ui.settings.SettingsViewModel
import com.focusgate.app.ui.stats.StatsScreen
import com.focusgate.app.ui.stats.StatsViewModel
import com.focusgate.app.util.PermissionUtils
import androidx.compose.ui.platform.LocalContext

@Composable
fun FocusGateNavHost(container: AppContainer, navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val startDestination = if (PermissionUtils.isAccessibilityServiceEnabled(context)) {
        Routes.HOME
    } else {
        Routes.ONBOARDING
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onContinue = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(container.statsRepository, container.settingsDataStore)
            )
            HomeScreen(
                viewModel = viewModel,
                onStartPractice = { navController.navigate(Routes.PRACTICE_CHALLENGE) },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.PRACTICE_CHALLENGE) {
            val viewModel: ChallengeViewModel = viewModel(
                factory = ChallengeViewModel.Factory(
                    container.questionRepository,
                    container.statsRepository,
                    container.settingsDataStore,
                    triggeredByInstagram = false
                )
            )
            ChallengeScreen(
                viewModel = viewModel,
                primaryActionLabel = "Volver al inicio",
                onPrimaryAction = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
                onAbandon = { navController.popBackStack() }
            )
        }

        composable(Routes.STATS) {
            val viewModel: StatsViewModel = viewModel(
                factory = StatsViewModel.Factory(container.statsRepository)
            )
            StatsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(container.settingsDataStore, container.statsRepository)
            )
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
