package com.focusgate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One answered question, kept for the statistics screen (per-category accuracy, trends). */
@Entity(tableName = "attempts")
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val questionId: String,
    val category: String,
    val difficulty: String,
    val wasCorrect: Boolean,
    val timestampMillis: Long
)

/** One full 10-question (or however many configured) challenge, triggered either by an
 *  Instagram open attempt or started manually from Home for practice. */
@Entity(tableName = "challenge_sessions")
data class ChallengeSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtMillis: Long,
    val finishedAtMillis: Long?,
    val completed: Boolean,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val triggeredByInstagram: Boolean
)

/** One row per calendar day (yyyy-MM-dd), aggregated for the stats dashboard and streaks. */
@Entity(tableName = "daily_stats")
data class DailyStatEntity(
    @PrimaryKey val date: String,
    val instagramOpenAttempts: Int = 0,
    val challengesCompleted: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val challengeTimeMillis: Long = 0L
)
