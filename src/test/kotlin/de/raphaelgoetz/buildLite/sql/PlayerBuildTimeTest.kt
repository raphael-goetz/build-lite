package de.raphaelgoetz.buildLite.sql

import de.raphaelgoetz.buildLite.testsupport.TestDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class PlayerBuildTimeTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() = TestDatabase.connect()
    }

    @AfterEach
    fun cleanup() = TestDatabase.clear()

    @Test
    fun `addSqlBuildTimeSeconds accumulates seconds for the same day`() {
        val player = UUID.randomUUID()
        val world = UUID.randomUUID()
        val today = LocalDate.now()

        addSqlBuildTimeSeconds(player, world, today, 20)
        addSqlBuildTimeSeconds(player, world, today, 30)

        assertEquals(50L, getSqlBuildTimeByWorld(player)[world])
    }

    @Test
    fun `getSqlBuildTimeByWorld excludes buckets before the cutoff`() {
        val player = UUID.randomUUID()
        val world = UUID.randomUUID()
        val today = LocalDate.now()

        addSqlBuildTimeSeconds(player, world, today, 20)
        addSqlBuildTimeSeconds(player, world, today.minusDays(10), 40)

        assertEquals(20L, getSqlBuildTimeByWorld(player, since = today.minusDays(1))[world])
        assertEquals(60L, getSqlBuildTimeByWorld(player)[world])
    }

    @Test
    fun `getSqlBuildTimeByWorld groups separate worlds independently`() {
        val player = UUID.randomUUID()
        val worldOne = UUID.randomUUID()
        val worldTwo = UUID.randomUUID()
        val today = LocalDate.now()

        addSqlBuildTimeSeconds(player, worldOne, today, 10)
        addSqlBuildTimeSeconds(player, worldTwo, today, 25)

        val byWorld = getSqlBuildTimeByWorld(player)

        assertEquals(10L, byWorld[worldOne])
        assertEquals(25L, byWorld[worldTwo])
    }

    @Test
    fun `getSqlBuildTimeByWorld returns an empty map for a player with no time recorded`() {
        assertTrue(getSqlBuildTimeByWorld(UUID.randomUUID()).isEmpty())
    }

    @Test
    fun `batch sampler write accumulates every player in one pass`() {
        val firstPlayer = UUID.randomUUID()
        val secondPlayer = UUID.randomUUID()
        val world = UUID.randomUUID()
        val today = LocalDate.now()

        addSqlBuildTimeSeconds(
            listOf(
                BuildTimeCredit(firstPlayer, world, 20),
                BuildTimeCredit(secondPlayer, world, 20),
            ),
            today,
        )

        assertEquals(20L, getSqlBuildTimeByWorld(firstPlayer)[world])
        assertEquals(20L, getSqlBuildTimeByWorld(secondPlayer)[world])
    }
}
