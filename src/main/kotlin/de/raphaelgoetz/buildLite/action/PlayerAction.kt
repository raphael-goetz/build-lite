package de.raphaelgoetz.buildLite.action

import de.raphaelgoetz.buildLite.cache.PlayerCache
import de.raphaelgoetz.buildLite.sql.updateSqlPlayer
import de.raphaelgoetz.buildLite.world.toLoadableLocation
import org.bukkit.Location
import org.bukkit.entity.Player

fun Player.actionUpdateLastLocation(location: Location) {
    val loadableLocation = location.toLoadableLocation()

    loadableLocation?.let {
        updateSqlPlayer(location = it)
    }

    PlayerCache.refresh(this)
}

fun Player.actionEnableBuildMode() {
    updateSqlPlayer(buildMode = true)
    PlayerCache.refresh(this)
}

fun Player.actionEnableNightMode() {
    updateSqlPlayer(nightMode = true)
    PlayerCache.refresh(this)
}

fun Player.actionDisableBuildMode() {
    updateSqlPlayer(buildMode = false)
    PlayerCache.refresh(this)
}

fun Player.actionDisableNightMode() {
    updateSqlPlayer(nightMode = false)
    PlayerCache.refresh(this)
}