package com.focusgate.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.focusgate.app.data.datastore.AppSettings
import com.focusgate.app.data.datastore.SettingsDataStore
import com.focusgate.app.data.repository.OverallStats
import com.focusgate.app.data.repository.StatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val stats: OverallStats? = null,
    val settings: AppSettings = AppSettings()
)

class HomeViewModel(
    private val statsRepository: StatsRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        statsRepository.observeOverallStats(),
        settingsDataStore.settingsFlow
    ) { stats, settings -> HomeUiState(stats = stats, settings = settings) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleBlocking(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setBlockingEnabled(enabled) }
    }

    class Factory(
        private val statsRepository: StatsRepository,
        private val settingsDataStore: SettingsDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(statsRepository, settingsDataStore) as T
    }
}
