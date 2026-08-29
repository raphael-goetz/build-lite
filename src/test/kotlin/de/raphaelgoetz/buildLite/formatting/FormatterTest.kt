package de.raphaelgoetz.buildLite.formatting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FormatterTest {

    @Test
    fun `durations below one minute show seconds`() {
        assertEquals("0s", 0L.formatDuration())
        assertEquals("20s", 20L.formatDuration())
        assertEquals("59s", 59L.formatDuration())
    }

    @Test
    fun `durations below one hour show minutes and seconds`() {
        assertEquals("1m 0s", 60L.formatDuration())
        assertEquals("2m 5s", 125L.formatDuration())
    }

    @Test
    fun `durations of at least one hour stay compact`() {
        assertEquals("1h 0m", 3_600L.formatDuration())
        assertEquals("2h 15m", 8_100L.formatDuration())
    }
}
