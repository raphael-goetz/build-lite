package de.raphaelgoetz.buildLite.world

import org.bukkit.Location
import org.bukkit.World
import java.util.UUID

/**
 * Reserved UUID marking a [LoadableLocation] that points at the vanilla overworld
 * rather than one of build-lite's own UUID-named custom worlds. `UUID.randomUUID()`
 * always sets RFC 4122 version/variant bits, so it can never produce this all-zero
 * value -- safe as a sentinel with no chance of colliding with a real world.
 */
val OVERWORLD_UUID: UUID = UUID(0, 0)

/*
 * This is a loadable location to make locations of worlds that aren't loaded save
 * Will load the world if its unloaded
 */
data class LoadableLocation(
    val worldUuid: UUID,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
)

fun LoadableLocation.toLocation(world: World) = Location(world, x, y, z, yaw, pitch)

fun Location.toLoadableLocation(): LoadableLocation? {
    if (world.name == "world" || world.name == "world_nether" || world.name == "world_the_end") {
        return null
    }

    try {
        val uuid = UUID.fromString(world.name)
        return LoadableLocation(uuid, x, y, z, yaw, pitch)
    } catch (_: IllegalArgumentException) {
        return null
    }
}