package com.focusgate.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.focusgate.app.data.datastore.AppSettings
import com.focusgate.app.data.datastore.DifficultyMode
import com.focusgate.app.data.datastore.SettingsDataStore
import com.focusgate.app.data.datastore.ThemeMode
import com.focusgate.app.data.repository.StatsRepository
import com.focusgate.app.domain.model.Category
import com.focusgate.app.domain.model.Difficulty
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val statsRepository: StatsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setBlockingEnabled(enabled: Boolean) = update { settingsDataStore.setBlockingEnabled(enabled) }

    fun setQuestionsPerSession(count: Int) = update { settingsDataStore.setQuestionsPerSession(count) }

    fun setDifficultyMode(mode: DifficultyMode) = update { settingsDataStore.setDifficultyMode(mode) }

    fun setFixedDifficulty(difficulty: Difficulty) = update { settingsDataStore.setFixedDifficulty(difficulty) }

    fun toggleCategory(category: Category, enabled: Boolean) = update {
        val current = settings.value.enabledCategories
        val next = if (enabled) current + category else current - category
        settingsDataStore.setEnabledCategories(next.ifEmpty { setOf(category) })
    }

    fun setSoundEnabled(enabled: Boolean) = update { settingsDataStore.setSoundEnabled(enabled) }

    fun setThemeMode(mode: ThemeMode) = update { settingsDataStore.setThemeMode(mode) }

    fun resetStatistics() = update { statsRepository.resetAllStats() }

    private fun update(action: suspend () -> Unit) {
        viewModelScope.launch { action() }
    }

    class Factory(
        private val settingsDataStore: SettingsDataStore,
        private val statsRepository: StatsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(settingsDataStore, statsRepository) as T
    }
}
