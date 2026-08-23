package de.raphaelgoetz.buildLite.sql

import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.WorldState
import de.raphaelgoetz.buildLite.testsupport.TestDatabase
import de.raphaelgoetz.buildLite.testsupport.mockPlayer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.UUID

class PlayerCreditTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() = TestDatabase.connect()
    }

    @AfterEach
    fun cleanup() = TestDatabase.clear()

    @Test
    fun `getSqlPlayerCreditsFor returns an empty map for an empty input`() {
        assertTrue(getSqlPlayerCreditsFor(emptyList()).isEmpty())
    }

    @Test
    fun `getSqlPlayerCreditsFor groups credits by world in a single batch`() {
        val owner = mockPlayer()
        val contributorOne = UUID.randomUUID()
        val contributorTwo = UUID.randomUUID()
        val worldOne = owner.createSqlWorld("one", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)
        val worldTwo = owner.createSqlWorld("two", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)

        createSqlPlayerCredit(contributorOne, worldOne.uniqueId)
        createSqlPlayerCredit(contributorTwo, worldOne.uniqueId)
        createSqlPlayerCredit(contributorOne, worldTwo.uniqueId)

        val byWorld = getSqlPlayerCreditsFor(listOf(worldOne.uniqueId, worldTwo.uniqueId))

        assertEquals(2, byWorld[worldOne.uniqueId]?.size)
        assertEquals(1, byWorld[worldTwo.uniqueId]?.size)
    }

    @Test
    fun `getSqlPlayerCreditsFor does not return credits for worlds outside the requested set`() {
        val owner = mockPlayer()
        val worldOne = owner.createSqlWorld("one", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)
        val worldTwo = owner.createSqlWorld("two", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)
        createSqlPlayerCredit(UUID.randomUUID(), worldTwo.uniqueId)

        val byWorld = getSqlPlayerCreditsFor(listOf(worldOne.uniqueId))

        assertTrue(byWorld[worldTwo.uniqueId].isNullOrEmpty())
    }

    @Test
    fun `deleteSqlPlayerCredits removes every credit for that world`() {
        val owner = mockPlayer()
        val world = owner.createSqlWorld("one", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)
        createSqlPlayerCredit(UUID.randomUUID(), world.uniqueId)
        createSqlPlayerCredit(UUID.randomUUID(), world.uniqueId)

        world.deleteSqlPlayerCredits()

        assertTrue(getSqlPlayerCreditsFor(listOf(world.uniqueId)).isEmpty())
    }
}
