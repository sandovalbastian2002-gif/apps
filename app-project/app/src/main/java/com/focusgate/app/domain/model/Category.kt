package com.focusgate.app.domain.model

enum class Category(val storageKey: String, val displayNameEs: String) {
    ENGLISH("ENGLISH", "Inglés"),
    MATH("MATH", "Matemáticas"),
    GENERAL_CULTURE("GENERAL_CULTURE", "Cultura general"),
    BIOLOGY("BIOLOGY", "Biología"),
    PROGRAMMING("PROGRAMMING", "Programación"),
    PHYSICS("PHYSICS", "Física"),
    CHEMISTRY("CHEMISTRY", "Química"),
    FINANCE("FINANCE", "Finanzas y economía"),
    TECHNOLOGY("TECHNOLOGY", "Tecnología"),
    LOGIC("LOGIC", "Pensamiento lógico");

    companion object {
        fun fromStorageKey(key: String): Category =
            entries.firstOrNull { it.storageKey == key } ?: GENERAL_CULTURE
    }
}
