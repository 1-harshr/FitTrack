package com.harsh.fittrack.core.util

/** Formats elapsed seconds for the workout timer. */
object DurationFormatter {
    fun hhmmss(totalSeconds: Long): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return buildString {
            append(h.pad2()); append(':')
            append(m.pad2()); append(':')
            append(s.pad2())
        }
    }

    private fun Long.pad2(): String = if (this < 10) "0$this" else toString()
}
