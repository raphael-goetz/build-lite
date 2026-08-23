package de.raphaelgoetz.buildLite.sql

import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.WorldState
import de.raphaelgoetz.buildLite.testsupport.TestDatabase
import de.raphaelgoetz.buildLite.testsupport.mockPlayer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class PlayerFavoriteTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() = TestDatabase.connect()
    }

    @AfterEach
    fun cleanup() = TestDatabase.clear()

    @Test
    fun `hasSqlPlayerFavorite is false until a favorite is created`() {
        val player = mockPlayer()
        val world = player.createSqlWorld("spawn", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)

        assertFalse(player.hasSqlPlayerFavorite(world.uniqueId))

        player.createSqlPlayerFavorite(world.uniqueId)

        assertTrue(player.hasSqlPlayerFavorite(world.uniqueId))
    }

    @Test
    fun `createSqlPlayerFavorite is idempotent`() {
        val player = mockPlayer()
        val world = player.createSqlWorld("spawn", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)

        player.createSqlPlayerFavorite(world.uniqueId)
        player.createSqlPlayerFavorite(world.uniqueId)

        assertEquals(1, player.getSqlPlayerFavoriteWorldUuids().size)
    }

    @Test
    fun `deleteSqlPlayerFavorite removes only that player's favorite`() {
        val player = mockPlayer()
        val world = player.createSqlWorld("spawn", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)
        player.createSqlPlayerFavorite(world.uniqueId)

        player.deleteSqlPlayerFavorite(world.uniqueId)

        assertFalse(player.hasSqlPlayerFavorite(world.uniqueId))
    }

    @Test
    fun `getSqlPlayerFavoriteWorldUuids returns only the calling player's favorites`() {
        val player = mockPlayer()
        val otherPlayer = mockPlayer()
        val worldOne = player.createSqlWorld("one", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)
        val worldTwo = player.createSqlWorld("two", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)

        player.createSqlPlayerFavorite(worldOne.uniqueId)
        otherPlayer.createSqlPlayerFavorite(worldTwo.uniqueId)

        val favorites = player.getSqlPlayerFavoriteWorldUuids()

        assertEquals(setOf(worldOne.uniqueId), favorites)
    }
}
