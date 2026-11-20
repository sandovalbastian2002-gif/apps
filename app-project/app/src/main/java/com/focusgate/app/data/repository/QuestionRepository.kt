package com.focusgate.app.data.repository

import com.focusgate.app.data.datastore.AppSettings
import com.focusgate.app.data.datastore.DifficultyMode
import com.focusgate.app.data.local.dao.QuestionDao
import com.focusgate.app.data.local.entity.QuestionEntity
import com.focusgate.app.domain.model.Category
import com.focusgate.app.domain.model.Difficulty
import com.focusgate.app.domain.model.PresentedQuestion
import com.focusgate.app.domain.model.Question

class QuestionRepository(private val questionDao: QuestionDao) {

    /** Builds one challenge session respecting the user's settings:
     *  - [AppSettings.questionsPerSession] questions total.
     *  - Progressive difficulty (roughly 30% easy / 40% medium / 30% hard, by position) unless
     *    the user picked a single fixed difficulty for the whole session.
     *  - Categories drawn only from [AppSettings.enabledCategories], spread evenly at random.
     *  - Avoids repeating recently-shown questions while a fresh pool exists for that
     *    category/difficulty bucket. */
    suspend fun generateSession(settings: AppSettings): List<PresentedQuestion> {
        val total = settings.questionsPerSession.coerceIn(5, 20)
        val categories = settings.enabledCategories.ifEmpty { Category.entries.toSet() }.toList()
        val difficultySequence = buildDifficultySequence(total, settings)
        val categorySequence = buildCategorySequence(total, categories)

        val usedIds = mutableSetOf<String>()
        val now = System.currentTimeMillis()
        val chosen = mutableListOf<QuestionEntity>()

        for (i in 0 until total) {
            val category = categorySequence[i]
            val difficulty = difficultySequence[i]
            val entity = pickQuestion(category, difficulty, usedIds, categories)
            if (entity != null) {
                usedIds += entity.id
                chosen += entity
                questionDao.markShown(entity.id, now + i)
            }
        }

        return chosen.mapIndexed { index, entity -> entity.toPresented(index + 1, chosen.size) }
    }

    private suspend fun pickQuestion(
        category: Category,
        difficulty: Difficulty,
        usedIds: Set<String>,
        fallbackCategories: List<Category>
    ): QuestionEntity? {
        // 1) Exact category + difficulty.
        candidatesExcluding(category, difficulty, usedIds)?.let { return it }
        // 2) Same category, any difficulty.
        for (d in Difficulty.entries) {
            candidatesExcluding(category, d, usedIds)?.let { return it }
        }
        // 3) Any enabled category, same difficulty.
        for (c in fallbackCategories) {
            candidatesExcluding(c, difficulty, usedIds)?.let { return it }
        }
        // 4) Anything at all, not yet used in this session.
        for (c in fallbackCategories) {
            for (d in Difficulty.entries) {
                candidatesExcluding(c, d, usedIds)?.let { return it }
            }
        }
        return null
    }

    private suspend fun candidatesExcluding(
        category: Category,
        difficulty: Difficulty,
        usedIds: Set<String>
    ): QuestionEntity? {
        val pool = questionDao.candidatesFor(category.storageKey, difficulty.storageKey, limit = 8)
        val available = pool.filter { it.id !in usedIds }
        return available.randomOrNull()
    }

    private fun buildDifficultySequence(total: Int, settings: AppSettings): List<Difficulty> {
        if (settings.difficultyMode == DifficultyMode.FIXED) {
            return List(total) { settings.fixedDifficulty }
        }
        val easyCount = Math.round(total * 0.3).toInt().coerceAtLeast(1)
        val hardCount = Math.round(total * 0.3).toInt().coerceAtLeast(1)
        val mediumCount = (total - easyCount - hardCount).coerceAtLeast(0)
        return List(easyCount) { Difficulty.EASY } +
            List(mediumCount) { Difficulty.MEDIUM } +
            List(hardCount) { Difficulty.HARD }
    }

    private fun buildCategorySequence(total: Int, categories: List<Category>): List<Category> {
        if (categories.isEmpty()) return List(total) { Category.GENERAL_CULTURE }
        val repeated = mutableListOf<Category>()
        while (repeated.size < total) {
            repeated += categories.shuffled()
        }
        return repeated.take(total).shuffled()
    }

    private fun QuestionEntity.toPresented(position: Int, totalInSession: Int): PresentedQuestion {
        val question = Question(
            id = id,
            category = Category.fromStorageKey(category),
            difficulty = Difficulty.fromStorageKey(difficulty),
            language = language,
            text = this.question,
            options = options,
            correctIndex = correctIndex,
            explanation = explanation
        )
        val shuffledOrder = options.indices.shuffled()
        val shuffledOptions = shuffledOrder.map { options[it] }
        val shuffledCorrectIndex = shuffledOrder.indexOf(correctIndex)
        return PresentedQuestion(
            question = question,
            shuffledOptions = shuffledOptions,
            shuffledCorrectIndex = shuffledCorrectIndex,
            positionInSession = position,
            totalInSession = totalInSession
        )
    }
}
