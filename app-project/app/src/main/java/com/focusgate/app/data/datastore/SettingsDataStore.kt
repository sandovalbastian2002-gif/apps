package com.focusgate.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.focusgate.app.domain.model.Category
import com.focusgate.app.domain.model.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "focusgate_settings")

enum class DifficultyMode { PROGRESSIVE, FIXED }
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val blockingEnabled: Boolean = true,
    val questionsPerSession: Int = 10,
    val difficultyMode: DifficultyMode = DifficultyMode.PROGRESSIVE,
    val fixedDifficulty: Difficulty = Difficulty.MEDIUM,
    val enabledCategories: Set<Category> = Category.entries.toSet(),
    val soundEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val BLOCKING_ENABLED = booleanPreferencesKey("blocking_enabled")
        val QUESTIONS_PER_SESSION = intPreferencesKey("questions_per_session")
        val DIFFICULTY_MODE = stringPreferencesKey("difficulty_mode")
        val FIXED_DIFFICULTY = stringPreferencesKey("fixed_difficulty")
        val ENABLED_CATEGORIES = stringSetPreferencesKey("enabled_categories")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            blockingEnabled = prefs[Keys.BLOCKING_ENABLED] ?: true,
            questionsPerSession = prefs[Keys.QUESTIONS_PER_SESSION] ?: 10,
            difficultyMode = prefs[Keys.DIFFICULTY_MODE]?.let { runCatching { DifficultyMode.valueOf(it) }.getOrNull() }
                ?: DifficultyMode.PROGRESSIVE,
            fixedDifficulty = prefs[Keys.FIXED_DIFFICULTY]?.let { Difficulty.fromStorageKey(it) } ?: Difficulty.MEDIUM,
            enabledCategories = prefs[Keys.ENABLED_CATEGORIES]
                ?.map { Category.fromStorageKey(it) }
                ?.toSet()
                ?.ifEmpty { Category.entries.toSet() }
                ?: Category.entries.toSet(),
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.DARK
        )
    }

    suspend fun setBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BLOCKING_ENABLED] = enabled }
    }

    suspend fun setQuestionsPerSession(count: Int) {
        context.dataStore.edit { it[Keys.QUESTIONS_PER_SESSION] = count.coerceIn(5, 20) }
    }

    suspend fun setDifficultyMode(mode: DifficultyMode) {
        context.dataStore.edit { it[Keys.DIFFICULTY_MODE] = mode.name }
    }

    suspend fun setFixedDifficulty(difficulty: Difficulty) {
        context.dataStore.edit { it[Keys.FIXED_DIFFICULTY] = difficulty.storageKey }
    }

    suspend fun setEnabledCategories(categories: Set<Category>) {
        context.dataStore.edit { it[Keys.ENABLED_CATEGORIES] = categories.map { c -> c.storageKey }.toSet() }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}
