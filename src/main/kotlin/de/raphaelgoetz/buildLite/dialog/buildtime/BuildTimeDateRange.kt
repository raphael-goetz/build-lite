package de.raphaelgoetz.buildLite.dialog.buildtime

import java.time.LocalDate
import java.time.format.DateTimeParseException

internal data class BuildTimeDateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
)

/** A rolling seven-day window, including [today] as its final day. */
internal fun defaultBuildTimeDateRange(today: LocalDate = LocalDate.now()): BuildTimeDateRange =
    BuildTimeDateRange(today.minusDays(6), today)

/** Parses an inclusive ISO-8601 range and rejects reversed or invalid dates. */
internal fun parseBuildTimeDateRange(startText: String, endText: String): BuildTimeDateRange? {
    return try {
        val start = LocalDate.parse(startText)
        val end = LocalDate.parse(endText)
        if (end.isBefore(start)) null else BuildTimeDateRange(start, end)
    } catch (_: DateTimeParseException) {
        null
    }
}
