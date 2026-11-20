package com.focusgate.app.ui.stats.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class DayBar(val label: String, val value: Int)

/** A minimal, dependency-free bar chart (no charting library needed) for the last-7-days
 *  trend: one bar per day, height proportional to [DayBar.value]. */
@Composable
fun WeeklyBarChart(bars: List<DayBar>, barColor: Color, modifier: Modifier = Modifier) {
    val maxValue = (bars.maxOfOrNull { it.value } ?: 0).coerceAtLeast(1)
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            if (bars.isEmpty()) return@Canvas
            val barCount = bars.size
            val spacing = size.width * 0.03f
            val barWidth = (size.width - spacing * (barCount - 1)) / barCount

            bars.forEachIndexed { index, bar ->
                val left = index * (barWidth + spacing)
                val barHeightRatio = bar.value / maxValue.toFloat()
                val barHeight = size.height * barHeightRatio.coerceIn(0.03f, 1f)
                val top = size.height - barHeight

                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }
        Row7Labels(bars.map { it.label })
    }
}

@Composable
private fun Row7Labels(labels: List<String>) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEach { label ->
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
