package com.focusgate.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.focusgate.app.data.repository.OverallStats
import com.focusgate.app.data.repository.StatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatsViewModel(private val statsRepository: StatsRepository) : ViewModel() {

    val stats: StateFlow<OverallStats?> = statsRepository.observeOverallStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun resetStats() {
        viewModelScope.launch { statsRepository.resetAllStats() }
    }

    class Factory(private val statsRepository: StatsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StatsViewModel(statsRepository) as T
    }
}
