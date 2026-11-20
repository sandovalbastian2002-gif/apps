package com.focusgate.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.focusgate.app.data.local.dao.QuestionDao
import com.focusgate.app.data.local.dao.StatsDao
import com.focusgate.app.data.local.entity.AttemptEntity
import com.focusgate.app.data.local.entity.ChallengeSessionEntity
import com.focusgate.app.data.local.entity.DailyStatEntity
import com.focusgate.app.data.local.entity.QuestionEntity

@Database(
    entities = [
        QuestionEntity::class,
        AttemptEntity::class,
        ChallengeSessionEntity::class,
        DailyStatEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun statsDao(): StatsDao

    companion object {
        const val DATABASE_NAME = "focusgate.db"
    }
}
