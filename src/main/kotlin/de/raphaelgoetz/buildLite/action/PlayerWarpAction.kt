package de.raphaelgoetz.buildLite.action

import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.PREFIX
import de.raphaelgoetz.buildLite.player.checkPermission
import de.raphaelgoetz.buildLite.sql.RecordPlayerWarp
import de.raphaelgoetz.buildLite.sql.createSqlPlayerWarp
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerWarp
import de.raphaelgoetz.buildLite.world.toLoadableLocation
import org.bukkit.entity.Player

fun Player.actionWarpDelete(record: RecordPlayerWarp) {
    if (!checkPermission("build-lite.warp.delete")) return
    record.deleteSqlPlayerWarp()
    sendMessage(adventureText("$PREFIX Warp '$name' deleted.") {
        color = Colorization.LIME
    })
}

fun Player.actionWarpCreate(name: String, isPrivate: Boolean = false) {
    if (!checkPermission("build-lite.warp.create")) return

    location.toLoadableLocation()?.let { location ->
        createSqlPlayerWarp(location, name, isPrivate)
    }

    sendMessage(adventureText("$PREFIX Warp '$name' created.") {
        color = Colorization.LIME
    })
}