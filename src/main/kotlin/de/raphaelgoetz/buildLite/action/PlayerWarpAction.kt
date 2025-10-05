package de.raphaelgoetz.buildLite.action

import de.raphaelgoetz.buildLite.sql.RecordPlayerWarp
import de.raphaelgoetz.buildLite.sql.createSqlPlayerWarp
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerWarp
import de.raphaelgoetz.buildLite.world.toLoadableLocation
import org.bukkit.entity.Player

fun Player.actionWarpDelete(record: RecordPlayerWarp) {

    if (!hasPermission("build-lite.warp.delete") || !hasPermission("build-lite.*")) {
        sendMessage("You do not have permission to create this world.")
        return
    }

    record.deleteSqlPlayerWarp()
}

fun Player.actionWarpCreate(name: String, isPrivate: Boolean = false) {

    if (!hasPermission("build-lite.warp.create") || !hasPermission("build-lite.*")) {
        sendMessage("You do not have permission to create this world.")
        return
    }

    location.toLoadableLocation()?.let { location ->
        createSqlPlayerWarp(location, name, isPrivate)
    }
}