package com.focusgate.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusgate.app.data.local.entity.QuestionEntity

@Dao
interface QuestionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun count(): Int

    /** Candidates for a given category/difficulty, oldest-shown-first so repeats are
     *  avoided while there is still a fresh (never shown, lastShownAtMillis = 0) pool. */
    @Query(
        "SELECT * FROM questions WHERE category = :category AND difficulty = :difficulty " +
            "ORDER BY lastShownAtMillis ASC LIMIT :limit"
    )
    suspend fun candidatesFor(category: String, difficulty: String, limit: Int): List<QuestionEntity>

    @Update
    suspend fun update(question: QuestionEntity)

    @Query("UPDATE questions SET lastShownAtMillis = :atMillis WHERE id = :id")
    suspend fun markShown(id: String, atMillis: Long)

    @Query("SELECT DISTINCT category FROM questions")
    suspend fun allCategories(): List<String>
}
