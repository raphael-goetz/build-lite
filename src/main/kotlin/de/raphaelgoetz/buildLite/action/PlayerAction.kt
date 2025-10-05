package de.raphaelgoetz.buildLite.action

import de.raphaelgoetz.buildLite.sql.updateSqlPlayer
import de.raphaelgoetz.buildLite.world.toLoadableLocation
import org.bukkit.Location
import org.bukkit.entity.Player

fun Player.actionUpdateLastLocation(location: Location) {
    val loadableLocation = location.toLoadableLocation()

    loadableLocation?.let {
        updateSqlPlayer(location = it)
    }

}