package de.raphaelgoetz.buildLite.action

import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.PREFIX
import de.raphaelgoetz.buildLite.player.checkPermission
import de.raphaelgoetz.buildLite.sanitiser.sanitiseGroupInput
import de.raphaelgoetz.buildLite.sanitiser.sanitiseNameInput
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.createSqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.createSqlWorld
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.hasSqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.WorldState
import de.raphaelgoetz.buildLite.sql.types.toGenerator
import de.raphaelgoetz.buildLite.sql.updateSqlWorld
import de.raphaelgoetz.buildLite.world.LoadableLocation
import de.raphaelgoetz.buildLite.world.WorldCreator
import de.raphaelgoetz.buildLite.world.WorldLoader
import de.raphaelgoetz.buildLite.world.WorldMigrator
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

fun Player.actionWorldCreate(name: String, group: String, generator: WorldGenerator, state: WorldState) {
    if (!checkPermission("build-lite.world.create")) return

    val correctName = name.sanitiseNameInput()
    val correctGroup = group.sanitiseGroupInput()

    val record = createSqlWorld(correctName, correctGroup, generator, state)
    WorldCreator.create(record.uniqueId.toString(), record.generator.toGenerator())
    sendMessage(adventureText("$PREFIX World '${record.name}' (in group: '${record.group}') created successfully.") {
        color = Colorization.LIME
    })
}

fun Player.actionWorldMigrate(
    migrateWorld: String, name: String, group: String, generator: WorldGenerator, state: WorldState
) {
    if (!checkPermission("build-lite.world.migrate")) return


    val correctName = name.sanitiseNameInput()
    val correctGroup = group.sanitiseGroupInput()

    val record = createSqlWorld(correctName, correctGroup, generator, state)
    try {
        WorldMigrator.migrate(migrateWorld, record.uniqueId, record.generator.toGenerator())
        sendMessage(adventureText("$PREFIX World '$migrateWorld' migrated to '${record.name}' (in group: '${record.group}')") {
            color = Colorization.LIME
        })
    } catch (_: Exception) {
        sendMessage(adventureText("$PREFIX Migration of world '$migrateWorld' failed.") {
            color = Colorization.RED
        })
    }
}

fun Player.actionWorldDelete(world: RecordWorld) {
    if (!checkPermission("build-lite.world.delete")) return
    WorldLoader.lazyDelete(world)

    for (onlinePlayer in Bukkit.getOnlinePlayers()) {
        onlinePlayer.sendMessage(adventureText("$PREFIX $name deleted the world '${world.name}'.") {
            color = Colorization.LIME
        })
    }
}

fun Player.actionWorldUpdate(
    world: RecordWorld,
    name: String? = null,
    group: String? = null,
    state: WorldState? = null,
    generator: WorldGenerator? = null,
) {
    if (!checkPermission("build-lite.world.update")) return
    world.updateSqlWorld(
        name,
        group,
        null,
        state,
        generator,
    )
    sendMessage(adventureText("$PREFIX World '${world.name}' updated successfully.") {
        color = Colorization.LIME
    })
}

fun Player.actionUpdateWorldSpawn(recordWorld: RecordWorld) {
    if (!checkPermission("build-lite.world.update")) return

    try {
        val currentWorldUUid = UUID.fromString(location.world.name)
        if (currentWorldUUid != recordWorld.uniqueId) {
            sendMessage(adventureText("$PREFIX You must be in the world you are trying to update.") {
                color = Colorization.RED
            })
            return
        }

        recordWorld.updateSqlWorld(
            spawn = LoadableLocation(
                recordWorld.uniqueId,
                location.x,
                location.y,
                location.z,
                location.yaw,
                location.pitch,
            )
        )

        sendMessage(adventureText("$PREFIX The spawn of the world '${world.name}' has been updated successfully.") {
            color = Colorization.LIME
        })
    } catch (_: Exception) {
        sendMessage(adventureText("$PREFIX An unexpected error occurred while updating the worlds spawn.") {
            color = Colorization.LIME
        })
    }
}

fun Player.actionWorldFavoriteToggle(recordWorld: RecordWorld) {
    if (!checkPermission("build-lite.world.favorite")) return

    val isFavorite = hasSqlPlayerFavorite(recordWorld.uniqueId)
    if (isFavorite) {
        deleteSqlPlayerFavorite(recordWorld.uniqueId)
        sendMessage(adventureText("$PREFIX World '${world.name}' removed from favorites.") {
            color = Colorization.LIME
        })
    } else {
        createSqlPlayerFavorite(recordWorld.uniqueId)
        sendMessage(adventureText("$PREFIX World '${world.name}' added to favorites.") {
            color = Colorization.LIME
        })
    }
}