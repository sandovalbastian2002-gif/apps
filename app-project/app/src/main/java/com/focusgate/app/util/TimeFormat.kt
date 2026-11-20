package com.focusgate.app.util

import java.util.concurrent.TimeUnit

object TimeFormat {

    /** Short human string for small durations, e.g. "0:42" or "3:05" (used for a single
     *  challenge's elapsed time). */
    fun minutesSeconds(durationMillis: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    /** Longer cumulative string, e.g. "2h 15min" or "45min" (used for aggregated stats). */
    fun hoursMinutes(durationMillis: Long): String {
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}min" else "${minutes}min"
    }
}
