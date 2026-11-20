package com.focusgate.app

import android.content.Context
import androidx.room.Room
import com.focusgate.app.data.datastore.SettingsDataStore
import com.focusgate.app.data.local.AppDatabase
import com.focusgate.app.data.local.seed.QuestionSeeder
import com.focusgate.app.data.repository.QuestionRepository
import com.focusgate.app.data.repository.StatsRepository

/** Simple, hand-rolled dependency container (no DI framework): the app is small enough that
 *  one object graph built once at startup is easier to follow than annotation-driven DI. */
class AppContainer(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    ).build()

    val settingsDataStore = SettingsDataStore(context.applicationContext)
    val questionRepository = QuestionRepository(database.questionDao())
    val statsRepository = StatsRepository(database.statsDao())
    val questionSeeder = QuestionSeeder(context.applicationContext, database.questionDao())
}
