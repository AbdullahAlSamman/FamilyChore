package org.aals.family.chore.core.domain.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Parse a `yyyy-MM-DD` string to epoch millis at start of given date in current timezone.
 * Returns null on malformed input.
 */
fun parseDateToEpochMillis(date: String): Long? {
    val parts = date.split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    return try {
        LocalDateTime(year, month, day, 0, 0, 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    } catch (e: Exception) {
        null
    }
}

/**
 * Format epoch millis to `yyyy-MM-dd` in current system timezone (or optional specified timezone).
 */
fun formatEpochMillisToDate(millis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
    val dt = instant.toLocalDateTime(timeZone)
    val year = dt.year
    val month = (dt.month.ordinal + 1).toString().padStart(2, '0')
    val day = dt.dayOfMonth.toString().padStart(2, '0')
    return "$year-$month-$day"
}

/**
 * Parse `HH:mm` (e.g. `14:00`) to minutes since midnight.
 * Returns null on malformed input.
 */
fun String.toMinutesOfDay(): Int? {
    if (isBlank()) return null
    val parts = split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    return if (h in 0..23 && m in 0..59) h * 60 + m else null
}

/**
 * Parse `HH:mm` string to (hour, minute) pair.
 * Defaults to (12, 0) on empty or invalid input.
 */
fun parseTimeString(timeStr: String): Pair<Int, Int> {
    val mins = timeStr.toMinutesOfDay() ?: return 12 to 0
    return (mins / 60) to (mins % 60)
}

/**
 * Returns epoch millis for 00:00:00 of today in the given timezone.
 */
fun startOfTodayEpochMillis(nowMillis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): Long {
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(nowMillis)
    val dt = instant.toLocalDateTime(timeZone)
    return LocalDateTime(dt.year, dt.month.ordinal + 1, dt.day, 0, 0, 0).toInstant(timeZone).toEpochMilliseconds()
}

/**
 * Returns true if the given date string (`yyyy-MM-dd`) represents a date before today.
 */
fun isDateInPast(dateStr: String, nowMillis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val dateMillis = parseDateToEpochMillis(dateStr) ?: return false
    val todayStartMillis = startOfTodayEpochMillis(nowMillis, timeZone)
    return dateMillis < todayStartMillis
}
