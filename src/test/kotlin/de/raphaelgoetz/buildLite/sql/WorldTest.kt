package de.raphaelgoetz.buildLite.sql

import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.WorldState
import de.raphaelgoetz.buildLite.testsupport.TestDatabase
import de.raphaelgoetz.buildLite.testsupport.mockPlayer
import de.raphaelgoetz.buildLite.world.LoadableLocation
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.UUID

class WorldTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() = TestDatabase.connect()
    }

    @AfterEach
    fun cleanup() = TestDatabase.clear()

    @Test
    fun `createSqlWorld persists and is retrievable by uuid`() {
        val creator = mockPlayer()
        val created = creator.createSqlWorld("spawn", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)

        val fetched = getSqlWorld(created.uniqueId)

        assertEquals("spawn", fetched.name)
        assertEquals("default", fetched.group)
        assertEquals(WorldGenerator.VOID, fetched.generator)
        assertEquals(WorldState.NOT_STARTED, fetched.state)
    }

    @Test
    fun `updateSqlWorld only overwrites provided fields`() {
        val creator = mockPlayer()
        val created = creator.createSqlWorld("spawn", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)

        val updated = created.updateSqlWorld(name = "renamed")

        assertEquals("renamed", updated.name)
        assertEquals("default", updated.group)
        assertEquals(WorldGenerator.VOID, updated.generator)
    }

    @Test
    fun `updateSqlWorld can move spawn location`() {
        val creator = mockPlayer()
        val created = creator.createSqlWorld("spawn", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)
        val newSpawn = LoadableLocation(created.uniqueId, 10.0, 20.0, 30.0, 1f, 2f)

        val updated = created.updateSqlWorld(spawn = newSpawn)

        assertEquals(10.0, updated.loadableSpawn.x)
        assertEquals(20.0, updated.loadableSpawn.y)
        assertEquals(30.0, updated.loadableSpawn.z)
    }

    @Test
    fun `deleteSqlWorld removes the row`() {
        val creator = mockPlayer()
        val created = creator.createSqlWorld("spawn", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)

        created.deleteSqlWorld()

        assertFalse(created.uniqueId.toString().isSqlWorld())
    }

    @Test
    fun `isSqlWorld is true only for uuids that exist in the table`() {
        val creator = mockPlayer()
        val created = creator.createSqlWorld("spawn", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)

        assertTrue(created.uniqueId.toString().isSqlWorld())
        assertFalse(UUID.randomUUID().toString().isSqlWorld())
    }

    @Test
    fun `isSqlWorld returns false for non-uuid strings instead of throwing`() {
        assertFalse("not-a-uuid".isSqlWorld())
        assertFalse("world".isSqlWorld())
    }

    @Test
    fun `toSqlWorldOrNull returns the matching record`() {
        val creator = mockPlayer()
        val created = creator.createSqlWorld("spawn", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)

        val found = created.uniqueId.toString().toSqlWorldOrNull()

        assertEquals(created.uniqueId, found?.uniqueId)
    }

    @Test
    fun `toSqlWorldOrNull returns null for an unknown uuid`() {
        assertNull(UUID.randomUUID().toString().toSqlWorldOrNull())
    }

    @Test
    fun `toSqlWorldOrNull returns null for a non-uuid string instead of throwing`() {
        assertNull("world_nether".toSqlWorldOrNull())
    }

    @Test
    fun `getAllSqlWorlds returns every registered world`() {
        val creator = mockPlayer()
        creator.createSqlWorld("one", "default", WorldGenerator.VOID, WorldState.NOT_STARTED)
        creator.createSqlWorld("two", "default", WorldGenerator.CHESS, WorldState.PLANING)

        val all = getAllSqlWorlds()

        assertEquals(2, all.size)
    }
}
