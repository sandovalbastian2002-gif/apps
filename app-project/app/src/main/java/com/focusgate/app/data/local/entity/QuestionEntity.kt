package com.focusgate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val category: String,
    val difficulty: String,
    val language: String,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int,
    val explanation: String,
    val lastShownAtMillis: Long = 0L
) {
    val options: List<String> get() = listOf(optionA, optionB, optionC, optionD)
}
