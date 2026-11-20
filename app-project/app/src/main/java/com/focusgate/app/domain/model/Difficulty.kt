package com.focusgate.app.domain.model

enum class Difficulty(val storageKey: String, val displayNameEs: String) {
    EASY("EASY", "Fácil"),
    MEDIUM("MEDIUM", "Intermedio"),
    HARD("HARD", "Difícil");

    companion object {
        fun fromStorageKey(key: String): Difficulty =
            entries.firstOrNull { it.storageKey == key } ?: EASY
    }
}
