package de.raphaelgoetz.buildLite.store

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import java.util.Optional

class LoadableWorld(var optionalWorld: Optional<World> = Optional.empty(), val name: String) {

    fun load() {
        val world = Bukkit.getWorld(name)

        if (world == null) {
            Bukkit.createWorld(WorldCreator(name))
        } else {
            this.optionalWorld = Optional.of(world)
        }
    }

    fun reload() {
        var world = Bukkit.getWorld(name)
        if (world == null) {
            this.optionalWorld = Optional.empty()
        } else {
            this.optionalWorld = Optional.of(world)
        }
    }

    fun getPlayers(): List<Player> {
        if (optionalWorld.isPresent) {
            return optionalWorld.get().players.toList()
        }

        return listOf()
    }
}