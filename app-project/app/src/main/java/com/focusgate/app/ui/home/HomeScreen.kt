package com.focusgate.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusgate.app.util.TimeFormat
import kotlin.random.Random

private val motivationalMessages = listOf(
    "Cada desafío que respondes es un minuto menos perdido en el scroll infinito.",
    "Tu mente también necesita entrenamiento. Hoy ya le diste un poco.",
    "Pequeñas pausas para aprender, grandes cambios en tu enfoque.",
    "El conocimiento que ganas aquí no desaparece cuando cierras la app.",
    "Estás cambiando un hábito automático por una elección consciente."
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartPractice: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stats = uiState.stats

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("FocusGate", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Tu puerta de entrada consciente a Instagram",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Configuración")
                }
            }
        }

        item {
            val message = remember(stats?.challengesCompleted) {
                motivationalMessages[Random.nextInt(motivationalMessages.size)]
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        item {
            BlockingStatusCard(
                blockingEnabled = uiState.settings.blockingEnabled,
                onToggle = viewModel::toggleBlocking
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    icon = Icons.Filled.CheckCircle,
                    label = "Desafíos hoy",
                    value = (stats?.today?.challengesCompleted ?: 0).toString(),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    icon = Icons.Filled.Whatshot,
                    label = "Racha",
                    value = "${stats?.currentStreakDays ?: 0} días",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    icon = Icons.Filled.Schedule,
                    label = "Aprendizaje acumulado",
                    value = TimeFormat.hoursMinutes(stats?.totalLearningTimeMillis ?: 0L),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    icon = Icons.Filled.Bolt,
                    label = "Intentos a Instagram",
                    value = (stats?.instagramOpenAttempts ?: 0).toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Button(
                onClick = onStartPractice,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Practicar un desafío ahora", style = MaterialTheme.typography.titleMedium)
            }
        }

        item {
            Button(onClick = onOpenStats, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Estadísticas")
            }
        }
    }
}

@Composable
private fun BlockingStatusCard(blockingEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (blockingEnabled) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = if (blockingEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(
                        if (blockingEnabled) "Bloqueo activo" else "Bloqueo desactivado",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (blockingEnabled) "Instagram te pedirá un desafío" else "Instagram se abrirá sin restricciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(checked = blockingEnabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
