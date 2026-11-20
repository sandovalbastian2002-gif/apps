package com.focusgate.app.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusgate.app.data.local.entity.DailyStatEntity
import com.focusgate.app.data.repository.CategoryAccuracy
import com.focusgate.app.ui.stats.charts.DayBar
import com.focusgate.app.ui.stats.charts.WeeklyBarChart
import com.focusgate.app.ui.theme.AccentMint
import com.focusgate.app.util.TimeFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(viewModel: StatsViewModel, onBack: () -> Unit) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Estadísticas", style = MaterialTheme.typography.headlineMedium)
            }
        }

        if (stats == null) return@LazyColumn

        val overall = stats!!

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard("Intentos a Instagram", overall.instagramOpenAttempts.toString(), Modifier)
                SummaryCard("Desafíos completados", overall.challengesCompleted.toString(), Modifier)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard("Preguntas respondidas", overall.questionsAnswered.toString(), Modifier)
                SummaryCard("% de aciertos", "${overall.accuracyPercent}%", Modifier)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard("Racha actual", "${overall.currentStreakDays} días", Modifier)
                SummaryCard("Tiempo invertido", TimeFormat.hoursMinutes(overall.totalLearningTimeMillis), Modifier)
            }
        }

        item {
            SectionCard(title = "Últimos 7 días (preguntas respondidas)") {
                val bars = lastNDaysBars(overall.recent7Days, days = 7)
                WeeklyBarChart(bars = bars, barColor = MaterialTheme.colorScheme.primary)
            }
        }

        item {
            SectionCard(title = "Últimos 30 días (desafíos completados)") {
                val bars = lastNDaysBars(
                    overall.recent30Days,
                    days = 30,
                    valueSelector = { it.challengesCompleted }
                )
                WeeklyBarChart(bars = bars.chunkedAverage(), barColor = AccentMint)
            }
        }

        item {
            SectionCard(title = "Rendimiento por categoría") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (overall.categoryAccuracy.isEmpty()) {
                        Text(
                            "Responde algunas preguntas para ver tu rendimiento por categoría.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    overall.categoryAccuracy.forEach { accuracy ->
                        CategoryAccuracyRow(accuracy)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun CategoryAccuracyRow(accuracy: CategoryAccuracy) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(accuracy.category.displayNameEs, style = MaterialTheme.typography.bodyLarge)
            Text("${accuracy.percentage}%", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { accuracy.percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50))
        )
    }
}

private fun lastNDaysBars(
    entities: List<DailyStatEntity>,
    days: Int,
    valueSelector: (DailyStatEntity) -> Int = { it.questionsAnswered }
): List<DayBar> {
    val byDate = entities.associateBy { it.date }
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val today = LocalDate.now()
    return (days - 1 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        val key = date.format(formatter)
        val entity = byDate[key]
        val label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es"))
            .take(1).uppercase()
        DayBar(label = label, value = entity?.let(valueSelector) ?: 0)
    }
}

/** Collapses a 30-point series into roughly weekly buckets so the chart stays readable. */
private fun List<DayBar>.chunkedAverage(bucketSize: Int = 5): List<DayBar> =
    chunked(bucketSize).map { chunk ->
        DayBar(label = chunk.first().label, value = chunk.sumOf { it.value })
    }
