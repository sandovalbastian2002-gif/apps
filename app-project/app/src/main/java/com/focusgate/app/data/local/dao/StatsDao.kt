package com.focusgate.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusgate.app.data.local.entity.AttemptEntity
import com.focusgate.app.data.local.entity.ChallengeSessionEntity
import com.focusgate.app.data.local.entity.DailyStatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

    @Insert
    suspend fun insertSession(session: ChallengeSessionEntity): Long

    @Query("UPDATE challenge_sessions SET finishedAtMillis = :finishedAt, completed = :completed, correctAnswers = :correct WHERE id = :id")
    suspend fun finishSession(id: Long, finishedAt: Long, completed: Boolean, correct: Int)

    @Insert
    suspend fun insertAttempt(attempt: AttemptEntity)

    @Insert
    suspend fun insertAttempts(attempts: List<AttemptEntity>)

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun dailyStat(date: String): DailyStatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyStat(stat: DailyStatEntity)

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :days")
    fun recentDailyStats(days: Int): Flow<List<DailyStatEntity>>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    suspend fun allDailyStatsDesc(): List<DailyStatEntity>

    @Query("SELECT COUNT(*) FROM challenge_sessions WHERE completed = 1")
    fun totalChallengesCompleted(): Flow<Int>

    @Query("SELECT COUNT(*) FROM challenge_sessions")
    fun totalInstagramAttempts(): Flow<Int>

    @Query(
        "SELECT category, COUNT(*) as total, SUM(CASE WHEN wasCorrect THEN 1 ELSE 0 END) as correct " +
            "FROM attempts GROUP BY category"
    )
    suspend fun accuracyByCategory(): List<CategoryAccuracyRow>

    @Query("SELECT COUNT(*) FROM attempts")
    fun totalQuestionsAnswered(): Flow<Int>

    @Query("SELECT COUNT(*) FROM attempts WHERE wasCorrect = 1")
    fun totalCorrectAnswers(): Flow<Int>

    @Query("DELETE FROM attempts")
    suspend fun clearAttempts()

    @Query("DELETE FROM challenge_sessions")
    suspend fun clearSessions()

    @Query("DELETE FROM daily_stats")
    suspend fun clearDailyStats()
}

data class CategoryAccuracyRow(
    val category: String,
    val total: Int,
    val correct: Int
)
