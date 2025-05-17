package de.raphaelgoetz.buildLite.store

import de.raphaelgoetz.astralis.world.createBuildingWorld
import de.raphaelgoetz.astralis.world.existingWorlds
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.Optional

class BuildServer(
    private val players: MutableList<BuildPlayer> = mutableListOf(),
    private val teleports: MutableList<TeleportQueue> = mutableListOf()
) {

    private val worlds: MutableList<BuildWorld> = initializeWorlds()

    fun reloadWorlds() = worlds.forEach { world -> world.reload() }

    fun createWorld(name: String) {
        val world = createBuildingWorld(name)
        val optionalWorld = Optional.ofNullable(world)

        val buildWorld = BuildWorld(LoadableWorld(optionalWorld, name))
        worlds.add(buildWorld)
    }

    fun deleteWorld(world: BuildWorld) {
        worlds.remove(world)
        world.delete()
    }

    fun asBuildPlayer(player: Player): BuildPlayer? {
        return players.find { (it.player.uniqueId == player.uniqueId) }
    }

    fun asBuildWorld(world: World): BuildWorld? {
        return worlds.find { (it.getWorldName() == world.name) }
    }

    fun asBuildWorld(worldName: String): BuildWorld? {
        return worlds.find { (it.getWorldName() == worldName) }
    }

    fun removePlayer(player: Player) {
        val buildPlayer = asBuildPlayer(player) ?: return
        players.remove(buildPlayer)
    }

    fun newPlayer(player: Player) {
        val buildPlayer = BuildPlayer(player)
        players.add(buildPlayer)
    }

    fun queue(buildPlayer: BuildPlayer, world: BuildWorld) {
        teleports.add(TeleportQueue(buildPlayer, world))
    }

    fun completeQueue(world: World) {
        val matches = teleports.filter { it.world.getWorldName() == world.name }
        matches.forEach { teleport ->
            val player = teleport.player.player
            player.teleport(world.spawnLocation)
        }

        teleports.removeAll(matches)
    }

    fun getCategorisedWorlds(): HashMap<String, MutableList<BuildWorld>> {
        val result = HashMap<String, MutableList<BuildWorld>>()

        for (world in worlds) {
            val group = world.group

            if (result.containsKey(group)) {
                val list = result.get(group)  ?: continue
                list += world
                continue
            }

            result.put(group, mutableListOf(world))
        }

        return result
    }

    private fun initializeWorlds(): MutableList<BuildWorld> {
        val list = mutableListOf<BuildWorld>()

        for (existing in existingWorlds) {
          val bukkitWorld = Bukkit.getWorld(existing)
          val optional = if (bukkitWorld == null) Optional.empty() else Optional.of(bukkitWorld)
          val name = existing

          val loadableWorld = LoadableWorld(optional, name)
          val buildWorld = BuildWorld(loadableWorld)
          list.add(buildWorld)
        }

        return list
    }

}