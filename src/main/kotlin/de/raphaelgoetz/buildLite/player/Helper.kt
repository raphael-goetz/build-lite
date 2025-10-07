package de.raphaelgoetz.buildLite.player

import org.bukkit.entity.Player

fun Player.hasWorldEnterPermission(name: String, group: String): Boolean {

    if (hasPermission("build-lite.*")) {
        return true
    }

    if (hasPermission("build-lite.world.enter.*")) {
        return true
    }

    if (hasPermission("build-lite.world.enter.$group.*")) {
        return true
    }

    if (hasPermission("build-lite.world.enter.$group.$name")) {
        return true
    }

    return false
}