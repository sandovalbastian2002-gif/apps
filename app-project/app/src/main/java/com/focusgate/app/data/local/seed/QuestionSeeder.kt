package com.focusgate.app.data.local.seed

import android.content.Context
import com.focusgate.app.data.local.dao.QuestionDao
import com.focusgate.app.data.local.entity.QuestionEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Loads the bundled, offline question bank (one JSON file per category under
 *  assets/questions/) into Room the first time the app runs. The app never needs a
 *  network connection to show questions. */
class QuestionSeeder(
    private val context: Context,
    private val questionDao: QuestionDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val assetFiles = listOf(
        "questions/english.json",
        "questions/math.json",
        "questions/general_culture.json",
        "questions/biology.json",
        "questions/programming.json",
        "questions/physics.json",
        "questions/chemistry.json",
        "questions/finance.json",
        "questions/technology.json",
        "questions/logic.json"
    )

    suspend fun seedIfNeeded() {
        if (questionDao.count() > 0) return

        val entities = mutableListOf<QuestionEntity>()
        for (path in assetFiles) {
            runCatching {
                val text = context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val parsed = json.decodeFromString<List<QuestionJson>>(text)
                entities += parsed.map { it.toEntity() }
            }
        }
        if (entities.isNotEmpty()) {
            questionDao.insertAll(entities)
        }
    }
}

@Serializable
data class QuestionJson(
    val id: String,
    val category: String,
    val difficulty: String,
    val language: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
) {
    fun toEntity(): QuestionEntity = QuestionEntity(
        id = id,
        category = category,
        difficulty = difficulty,
        language = language,
        question = question,
        optionA = options.getOrElse(0) { "" },
        optionB = options.getOrElse(1) { "" },
        optionC = options.getOrElse(2) { "" },
        optionD = options.getOrElse(3) { "" },
        correctIndex = correctIndex,
        explanation = explanation
    )
}
