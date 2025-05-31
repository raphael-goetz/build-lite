package de.raphaelgoetz.buildLite.store

import de.raphaelgoetz.buildLite.record.WorldRecord
import de.raphaelgoetz.buildLite.record.WorldState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class BuildWorldTest {
    
    @Test
    fun testBuildWorldProperties() {
        // Arrange
        val worldId = UUID.randomUUID()
        val worldRecord = WorldRecord(worldId, "testName", "testGroup", WorldState.ACTIVE)
        
        // Act
        val buildWorld = BuildWorld(worldRecord)
        
        // Assert
        assertEquals("testName", buildWorld.name)
        assertEquals("testGroup", buildWorld.group)
        assertEquals(worldId.toString(), buildWorld.worldIdentifier)
        assertEquals("testGroup_testName", buildWorld.displayIdentifier)
        assertEquals(WorldState.ACTIVE, buildWorld.state)
        assertEquals(false, buildWorld.hasPhysics)
    }
}