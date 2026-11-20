package com.focusgate.app.ui.challenge

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusgate.app.domain.model.ChallengeOutcome
import com.focusgate.app.domain.model.PresentedQuestion
import com.focusgate.app.ui.theme.AccentAmber
import com.focusgate.app.ui.theme.AccentCoral
import com.focusgate.app.ui.theme.AccentMint
import com.focusgate.app.util.TimeFormat

@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    onAbandon: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingState()
            uiState.isFinished && uiState.outcome != null ->
                CompletionScreen(
                    outcome = uiState.outcome!!,
                    primaryActionLabel = primaryActionLabel,
                    onPrimaryAction = onPrimaryAction
                )
            else -> QuestionState(
                uiState = uiState,
                onSelectOption = viewModel::selectOption,
                onContinue = viewModel::continueAfterFeedback,
                onAbandon = onAbandon
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun QuestionState(
    uiState: ChallengeViewModel.UiState,
    onSelectOption: (Int) -> Unit,
    onContinue: () -> Unit,
    onAbandon: (() -> Unit)?
) {
    val question = uiState.currentQuestion ?: return
    val progress by animateFloatAsState(
        targetValue = (uiState.currentIndex).toFloat() / uiState.questions.size.coerceAtLeast(1),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Pregunta ${question.positionInSession}/${question.totalInSession}",
                style = MaterialTheme.typography.titleMedium
            )
            if (onAbandon != null) {
                Text(
                    "Salir",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onAbandon() }
                )
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50))
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Badge(text = question.question.category.displayNameEs, color = MaterialTheme.colorScheme.primary)
            Badge(text = question.question.difficulty.displayNameEs, color = difficultyColor(question.question.difficulty.storageKey))
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Text(
                text = question.question.text,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.shuffledOptions.forEachIndexed { index, option ->
                OptionRow(
                    text = option,
                    state = optionState(uiState, index),
                    enabled = !uiState.showFeedback,
                    onClick = { onSelectOption(index) }
                )
            }
        }

        AnimatedContent(targetState = uiState.showFeedback, label = "feedback") { showFeedback ->
            if (showFeedback) {
                FeedbackPanel(
                    isCorrect = uiState.isCurrentAnswerCorrect == true,
                    explanation = question.question.explanation,
                    correctOptionText = question.shuffledOptions[question.shuffledCorrectIndex],
                    onContinue = onContinue
                )
            }
        }
    }
}

private enum class OptionVisualState { NEUTRAL, CORRECT, INCORRECT_SELECTED }

private fun optionState(uiState: ChallengeViewModel.UiState, index: Int): OptionVisualState {
    val question = uiState.currentQuestion ?: return OptionVisualState.NEUTRAL
    if (!uiState.showFeedback) return OptionVisualState.NEUTRAL
    val correctIndex = question.shuffledCorrectIndex
    val selected = uiState.selectedOptionIndex
    return when {
        index == correctIndex -> OptionVisualState.CORRECT
        index == selected -> OptionVisualState.INCORRECT_SELECTED
        else -> OptionVisualState.NEUTRAL
    }
}

@Composable
private fun OptionRow(text: String, state: OptionVisualState, enabled: Boolean, onClick: () -> Unit) {
    val borderColor = when (state) {
        OptionVisualState.CORRECT -> AccentMint
        OptionVisualState.INCORRECT_SELECTED -> AccentCoral
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (state == OptionVisualState.CORRECT) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentMint)
            }
        }
    }
}

@Composable
private fun FeedbackPanel(
    isCorrect: Boolean,
    explanation: String,
    correctOptionText: String,
    onContinue: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) AccentMint.copy(alpha = 0.15f) else AccentCoral.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (isCorrect) "¡Correcto!" else "No es esa. La respuesta correcta es: $correctOptionText",
                style = MaterialTheme.typography.titleMedium
            )
            Text(explanation, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text(if (isCorrect) "Continuar" else "Intentar de nuevo")
            }
        }
    }
}

@Composable
private fun CompletionScreen(
    outcome: ChallengeOutcome,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = AccentMint,
            modifier = Modifier.height(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("¡Desafío completado!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Respondiste ${outcome.correctAnswers}/${outcome.totalQuestions} preguntas al primer intento.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text(
                "Tiempo utilizado: ${TimeFormat.minutesSeconds(outcome.durationMillis)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Categorías practicadas: ${outcome.categoriesPracticed.joinToString { it.displayNameEs }}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Cada desafío es una pequeña victoria contra el scroll automático.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onPrimaryAction, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(primaryActionLabel, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

private fun difficultyColor(storageKey: String): Color = when (storageKey) {
    "EASY" -> AccentMint
    "MEDIUM" -> AccentAmber
    else -> AccentCoral
}

