package de.raphaelgoetz.buildLite.action

import de.raphaelgoetz.buildLite.sanitiser.sanitiseGroupInput
import de.raphaelgoetz.buildLite.sanitiser.sanitiseNameInput
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.createSqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.createSqlWorld
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.deleteSqlWorld
import de.raphaelgoetz.buildLite.sql.hasSqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.WorldState
import de.raphaelgoetz.buildLite.sql.types.toGenerator
import de.raphaelgoetz.buildLite.sql.updateSqlWorld
import de.raphaelgoetz.buildLite.world.LoadableLocation
import de.raphaelgoetz.buildLite.world.WorldCreator
import org.bukkit.entity.Player
import java.util.UUID

fun Player.actionWorldCreate(name: String, group: String, generator: WorldGenerator, state: WorldState) {
    if (!hasPermission("build-lite.world.create") || !hasPermission("build-lite.*")) {
        sendMessage("You do not have permission to create this world.")
        return
    }

    val correctName = name.sanitiseNameInput()
    val correctGroup = group.sanitiseGroupInput()

    val record = createSqlWorld(correctName, correctGroup, generator, state)
    WorldCreator.create(record.uniqueId.toString(), record.generator.toGenerator())
}

fun Player.actionWorldDelete(world: RecordWorld) {
    if (!hasPermission("build-lite.world.delete") || !hasPermission("build-lite.*")) {
        sendMessage("You do not have permission to create this world.")
        return
    }

    world.deleteSqlWorld()
}

fun Player.actionWorldUpdate(
    world: RecordWorld,
    name: String? = null,
    group: String? = null,
    physicsEnabled: Boolean? = null,
    state: WorldState? = null,
    generator: WorldGenerator? = null,
    spawnLocation: LoadableLocation? = null,

) {
    if (!hasPermission("build-lite.world.update") || !hasPermission("build-lite.*")) {
        sendMessage("You do not have permission to create this world.")
        return
    }

    world.updateSqlWorld(
        name,
        group,
        spawnLocation,
        physicsEnabled,
        state,
        generator,
    )
}

fun Player.actionWorldSpawn() {
    if (!hasPermission("build-lite.world.update") || !hasPermission("build-lite.*")) {
        sendMessage("You do not have permission to create this world.")
        return
    }

    //TODO Map bukkit world to record
}

fun Player.actionWorldFavoriteToggle(worldUUID: UUID) {
    if (!hasPermission("build-lite.world.favorite") || !hasPermission("build-lite.*")) {
        sendMessage("You do not have permission to create this world.")
        return
    }

    val isFavorite = hasSqlPlayerFavorite(worldUUID)
    println(isFavorite)
    if (isFavorite) {
        deleteSqlPlayerFavorite(worldUUID)
        sendMessage("You have unfavorited.")
        return
    }

    createSqlPlayerFavorite(worldUUID)
    sendMessage("You have favorited.")
}