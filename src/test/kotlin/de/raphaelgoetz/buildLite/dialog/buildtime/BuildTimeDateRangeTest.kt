package de.raphaelgoetz.buildLite.dialog.buildtime

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.stream.Stream

class BuildTimeDateRangeTest {

    @Test
    fun `default range contains exactly seven days including today`() {
        val today = LocalDate.of(2026, 8, 31)

        val range = defaultBuildTimeDateRange(today)

        assertEquals(LocalDate.of(2026, 8, 25), range.startDate)
        assertEquals(today, range.endDate)
        assertEquals(7, range.endDate.toEpochDay() - range.startDate.toEpochDay() + 1)
    }

    @ParameterizedTest
    @MethodSource("validRanges")
    fun `valid inclusive ISO date ranges are accepted`(
        startText: String,
        endText: String,
        expectedStart: LocalDate,
        expectedEnd: LocalDate,
    ) {
        assertEquals(
            BuildTimeDateRange(expectedStart, expectedEnd),
            parseBuildTimeDateRange(startText, endText),
        )
    }

    @ParameterizedTest
    @MethodSource("invalidRanges")
    fun `invalid date ranges are rejected`(startText: String, endText: String) {
        assertNull(parseBuildTimeDateRange(startText, endText))
    }

    companion object {
        @JvmStatic
        fun validRanges(): Stream<Arguments> = Stream.of(
            Arguments.of("2026-08-25", "2026-08-31", LocalDate.of(2026, 8, 25), LocalDate.of(2026, 8, 31)),
            Arguments.of("2026-08-31", "2026-08-31", LocalDate.of(2026, 8, 31), LocalDate.of(2026, 8, 31)),
            Arguments.of("2024-02-29", "2024-03-01", LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 1)),
            Arguments.of("2025-12-31", "2026-01-01", LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 1)),
        )

        @JvmStatic
        fun invalidRanges(): Stream<Arguments> = Stream.of(
            Arguments.of("", "2026-08-31"),
            Arguments.of("2026-08-25", ""),
            Arguments.of("2026-08-32", "2026-09-01"),
            Arguments.of("2026-02-29", "2026-03-01"),
            Arguments.of("2026-04-31", "2026-05-01"),
            Arguments.of("2026-08-31", "2026-08-25"),
            Arguments.of("2026-8-25", "2026-08-31"),
            Arguments.of("25.08.2026", "31.08.2026"),
            Arguments.of("2026-08-25T00:00", "2026-08-31"),
            Arguments.of("not-a-date", "2026-08-31"),
        )
    }
}
