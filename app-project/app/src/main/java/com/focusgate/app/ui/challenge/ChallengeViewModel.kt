package com.focusgate.app.ui.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.focusgate.app.blocker.UnlockManager
import com.focusgate.app.data.datastore.SettingsDataStore
import com.focusgate.app.data.repository.QuestionRepository
import com.focusgate.app.data.repository.StatsRepository
import com.focusgate.app.domain.model.ChallengeOutcome
import com.focusgate.app.domain.model.PresentedQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChallengeViewModel(
    private val questionRepository: QuestionRepository,
    private val statsRepository: StatsRepository,
    private val settingsDataStore: SettingsDataStore,
    private val triggeredByInstagram: Boolean
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val questions: List<PresentedQuestion> = emptyList(),
        val currentIndex: Int = 0,
        val selectedOptionIndex: Int? = null,
        val showFeedback: Boolean = false,
        val firstTryCorrectCount: Int = 0,
        val mistakeCount: Int = 0,
        val isFinished: Boolean = false,
        val outcome: ChallengeOutcome? = null,
        val sessionId: Long = 0L,
        val startTimeMillis: Long = 0L
    ) {
        val currentQuestion: PresentedQuestion? get() = questions.getOrNull(currentIndex)
        val isCurrentAnswerCorrect: Boolean?
            get() = if (!showFeedback) null else selectedOptionIndex == currentQuestion?.shuffledCorrectIndex
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        startSession()
    }

    private fun startSession() = viewModelScope.launch {
        val settings = settingsDataStore.settingsFlow.first()
        val questions = questionRepository.generateSession(settings)
        val sessionId = statsRepository.startSession(triggeredByInstagram, questions.size)
        _uiState.value = UiState(
            isLoading = false,
            questions = questions,
            sessionId = sessionId,
            startTimeMillis = System.currentTimeMillis()
        )
    }

    fun selectOption(optionIndex: Int) {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        if (state.showFeedback || state.isFinished) return

        val wasCorrect = optionIndex == question.shuffledCorrectIndex
        _uiState.update {
            it.copy(
                selectedOptionIndex = optionIndex,
                showFeedback = true,
                firstTryCorrectCount = it.firstTryCorrectCount + if (wasCorrect) 1 else 0,
                mistakeCount = it.mistakeCount + if (wasCorrect) 0 else 1
            )
        }
        viewModelScope.launch {
            statsRepository.recordAnswer(state.sessionId, question, wasCorrect)
        }
    }

    /** Advances to the next question on a correct answer; on an incorrect one, clears the
     *  feedback so the same question can be retried — a wrong answer never counts as "done". */
    fun continueAfterFeedback() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        if (!state.showFeedback) return

        val wasCorrect = state.selectedOptionIndex == question.shuffledCorrectIndex
        if (!wasCorrect) {
            _uiState.update { it.copy(selectedOptionIndex = null, showFeedback = false) }
            return
        }

        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.questions.size) {
            finishSession(state)
        } else {
            _uiState.update { it.copy(currentIndex = nextIndex, selectedOptionIndex = null, showFeedback = false) }
        }
    }

    private fun finishSession(state: UiState) = viewModelScope.launch {
        val durationMillis = System.currentTimeMillis() - state.startTimeMillis
        statsRepository.finishSession(
            sessionId = state.sessionId,
            totalAnswered = state.questions.size,
            correctAnswers = state.firstTryCorrectCount,
            completed = true,
            durationMillis = durationMillis
        )
        if (triggeredByInstagram) {
            UnlockManager.grantUnlock()
        }
        val outcome = ChallengeOutcome(
            totalQuestions = state.questions.size,
            correctAnswers = state.firstTryCorrectCount,
            durationMillis = durationMillis,
            categoriesPracticed = state.questions.map { it.question.category }.toSet()
        )
        _uiState.update { it.copy(isFinished = true, outcome = outcome) }
    }

    class Factory(
        private val questionRepository: QuestionRepository,
        private val statsRepository: StatsRepository,
        private val settingsDataStore: SettingsDataStore,
        private val triggeredByInstagram: Boolean
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChallengeViewModel(questionRepository, statsRepository, settingsDataStore, triggeredByInstagram) as T
        }
    }
}
