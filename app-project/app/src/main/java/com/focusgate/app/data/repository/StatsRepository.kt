package com.focusgate.app.data.repository

import com.focusgate.app.data.local.dao.CategoryAccuracyRow
import com.focusgate.app.data.local.dao.StatsDao
import com.focusgate.app.data.local.entity.AttemptEntity
import com.focusgate.app.data.local.entity.ChallengeSessionEntity
import com.focusgate.app.data.local.entity.DailyStatEntity
import com.focusgate.app.domain.model.Category
import com.focusgate.app.domain.model.PresentedQuestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CategoryAccuracy(val category: Category, val total: Int, val correct: Int) {
    val percentage: Int get() = if (total == 0) 0 else Math.round(correct * 100f / total)
}

data class OverallStats(
    val instagramOpenAttempts: Int,
    val challengesCompleted: Int,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val accuracyPercent: Int,
    val currentStreakDays: Int,
    val bestCategory: Category?,
    val recent7Days: List<DailyStatEntity>,
    val recent30Days: List<DailyStatEntity>,
    val categoryAccuracy: List<CategoryAccuracy>,
    val totalLearningTimeMillis: Long
) {
    val today: DailyStatEntity?
        get() {
            val todayKey = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            return recent7Days.firstOrNull { it.date == todayKey }
        }
}

class StatsRepository(private val statsDao: StatsDao) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private fun today(): String = LocalDate.now().format(dateFormatter)

    /** Call when an Instagram-open attempt starts a new forced challenge. Returns the new
     *  session id, used later to record answers and finish the session. */
    suspend fun startSession(triggeredByInstagram: Boolean, totalQuestions: Int): Long {
        val sessionId = statsDao.insertSession(
            ChallengeSessionEntity(
                startedAtMillis = System.currentTimeMillis(),
                finishedAtMillis = null,
                completed = false,
                totalQuestions = totalQuestions,
                correctAnswers = 0,
                triggeredByInstagram = triggeredByInstagram
            )
        )
        if (triggeredByInstagram) {
            bumpDaily { it.copy(instagramOpenAttempts = it.instagramOpenAttempts + 1) }
        }
        return sessionId
    }

    suspend fun recordAnswer(sessionId: Long, question: PresentedQuestion, wasCorrect: Boolean) {
        statsDao.insertAttempt(
            AttemptEntity(
                sessionId = sessionId,
                questionId = question.question.id,
                category = question.question.category.storageKey,
                difficulty = question.question.difficulty.storageKey,
                wasCorrect = wasCorrect,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun finishSession(
        sessionId: Long,
        totalAnswered: Int,
        correctAnswers: Int,
        completed: Boolean,
        durationMillis: Long
    ) {
        statsDao.finishSession(sessionId, System.currentTimeMillis(), completed, correctAnswers)
        bumpDaily {
            it.copy(
                challengesCompleted = it.challengesCompleted + if (completed) 1 else 0,
                questionsAnswered = it.questionsAnswered + totalAnswered,
                correctAnswers = it.correctAnswers + correctAnswers,
                challengeTimeMillis = it.challengeTimeMillis + durationMillis
            )
        }
    }

    private suspend fun bumpDaily(transform: (DailyStatEntity) -> DailyStatEntity) {
        val date = today()
        val current = statsDao.dailyStat(date) ?: DailyStatEntity(date = date)
        statsDao.upsertDailyStat(transform(current))
    }

    suspend fun resetAllStats() {
        statsDao.clearAttempts()
        statsDao.clearSessions()
        statsDao.clearDailyStats()
    }

    fun observeOverallStats(): Flow<OverallStats> =
        combine(
            statsDao.totalInstagramAttempts(),
            statsDao.totalChallengesCompleted(),
            statsDao.totalQuestionsAnswered(),
            statsDao.totalCorrectAnswers(),
            statsDao.recentDailyStats(30)
        ) { attempts, completed, answered, correct, last30 ->
            val last7 = last30.take(7)
            val accuracyRows = statsDao.accuracyByCategory()
            val categoryAccuracy = accuracyRows.map { row: CategoryAccuracyRow ->
                CategoryAccuracy(Category.fromStorageKey(row.category), row.total, row.correct)
            }.sortedByDescending { it.percentage }
            OverallStats(
                instagramOpenAttempts = attempts,
                challengesCompleted = completed,
                questionsAnswered = answered,
                correctAnswers = correct,
                accuracyPercent = if (answered == 0) 0 else Math.round(correct * 100f / answered),
                currentStreakDays = computeStreak(last30),
                bestCategory = categoryAccuracy.firstOrNull { it.total >= 3 }?.category,
                recent7Days = last7,
                recent30Days = last30,
                categoryAccuracy = categoryAccuracy,
                totalLearningTimeMillis = last30.sumOf { it.challengeTimeMillis }
            )
        }

    private fun computeStreak(recentDescending: List<DailyStatEntity>): Int {
        if (recentDescending.isEmpty()) return 0
        val byDate = recentDescending.associateBy { it.date }
        var streak = 0
        var cursor = LocalDate.now()
        while (true) {
            val key = cursor.format(dateFormatter)
            val stat = byDate[key]
            if (stat != null && stat.challengesCompleted > 0) {
                streak++
                cursor = cursor.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }
}
