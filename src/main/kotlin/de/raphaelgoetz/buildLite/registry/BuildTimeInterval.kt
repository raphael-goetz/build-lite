package de.raphaelgoetz.buildLite.registry

import java.time.LocalDate

enum class BuildTimeInterval(val text: String) {
    TODAY("Today"),
    WEEK("Last 7 Days"),
    MONTH("Last 30 Days"),
    ALL_TIME("All-Time");

    /** Earliest day (inclusive) to sum for this interval, or null for all-time. */
    fun cutoff(today: LocalDate = LocalDate.now()): LocalDate? = when (this) {
        TODAY -> today
        WEEK -> today.minusDays(6)
        MONTH -> today.minusDays(29)
        ALL_TIME -> null
    }
}
