package de.raphaelgoetz.buildLite

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

object BuildServer {
    val spawnLocation = Location(Bukkit.getWorld("world"), 0.0, 100.0, 0.0)

}

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