package com.focusgate.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusgate.app.data.datastore.DifficultyMode
import com.focusgate.app.data.datastore.ThemeMode
import com.focusgate.app.domain.model.Category
import com.focusgate.app.domain.model.Difficulty

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showResetConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Configuración", style = MaterialTheme.typography.headlineMedium)
            }
        }

        item {
            SettingsSection(title = "Bloqueo") {
                SettingsRow(label = "Activar bloqueo de Instagram") {
                    Switch(checked = settings.blockingEnabled, onCheckedChange = viewModel::setBlockingEnabled)
                }
            }
        }

        item {
            SettingsSection(title = "Preguntas por sesión: ${settings.questionsPerSession}") {
                Slider(
                    value = settings.questionsPerSession.toFloat(),
                    onValueChange = { viewModel.setQuestionsPerSession(it.toInt()) },
                    valueRange = 5f..20f,
                    steps = 14
                )
            }
        }

        item {
            SettingsSection(title = "Dificultad") {
                Column {
                    RadioRow(
                        label = "Progresiva (fácil → difícil)",
                        selected = settings.difficultyMode == DifficultyMode.PROGRESSIVE,
                        onClick = { viewModel.setDifficultyMode(DifficultyMode.PROGRESSIVE) }
                    )
                    RadioRow(
                        label = "Fija",
                        selected = settings.difficultyMode == DifficultyMode.FIXED,
                        onClick = { viewModel.setDifficultyMode(DifficultyMode.FIXED) }
                    )
                    if (settings.difficultyMode == DifficultyMode.FIXED) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            Difficulty.entries.forEach { difficulty ->
                                FilterChip(
                                    selected = settings.fixedDifficulty == difficulty,
                                    onClick = { viewModel.setFixedDifficulty(difficulty) },
                                    label = { Text(difficulty.displayNameEs) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(title = "Categorías de conocimiento") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Category.entries.forEach { category ->
                        SettingsRow(label = category.displayNameEs) {
                            Switch(
                                checked = category in settings.enabledCategories,
                                onCheckedChange = { checked -> viewModel.toggleCategory(category, checked) }
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(title = "Apariencia") {
                Column {
                    RadioRow("Oscuro", settings.themeMode == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) }
                    RadioRow("Claro", settings.themeMode == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }
                    RadioRow("Seguir al sistema", settings.themeMode == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                }
            }
        }

        item {
            SettingsSection(title = "Sonido") {
                SettingsRow(label = "Sonidos de la app") {
                    Switch(checked = settings.soundEnabled, onCheckedChange = viewModel::setSoundEnabled)
                }
            }
        }

        item {
            SettingsSection(title = "Datos") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!showResetConfirm) {
                        OutlinedButton(onClick = { showResetConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Reiniciar estadísticas")
                        }
                    } else {
                        Text(
                            "Esto borrará permanentemente tu historial de desafíos y estadísticas. ¿Confirmas?",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showResetConfirm = false }, modifier = Modifier.weight(1f)) {
                                Text("Cancelar")
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.resetStatistics()
                                    showResetConfirm = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Sí, borrar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun SettingsRow(label: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        trailing()
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
