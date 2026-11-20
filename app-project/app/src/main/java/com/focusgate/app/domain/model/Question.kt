package com.focusgate.app.domain.model

data class Question(
    val id: String,
    val category: Category,
    val difficulty: Difficulty,
    val language: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

/** A [Question] with its options shuffled for presentation; [shuffledCorrectIndex] points
 *  at the correct option's new position inside [shuffledOptions]. */
data class PresentedQuestion(
    val question: Question,
    val shuffledOptions: List<String>,
    val shuffledCorrectIndex: Int,
    val positionInSession: Int,
    val totalInSession: Int
)

data class ChallengeOutcome(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val durationMillis: Long,
    val categoriesPracticed: Set<Category>
)
