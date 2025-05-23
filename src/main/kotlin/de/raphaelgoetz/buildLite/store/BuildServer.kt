package de.raphaelgoetz.buildLite.store

import de.raphaelgoetz.astralis.world.createBuildingWorld
import de.raphaelgoetz.astralis.world.existingWorlds
import de.raphaelgoetz.buildLite.record.WorldState
import de.raphaelgoetz.buildLite.record.Worlds
import de.raphaelgoetz.buildLite.record.createWorldRecord
import de.raphaelgoetz.buildLite.record.getWorldRecords
import de.raphaelgoetz.buildLite.record.isWorldRecord
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class BuildServer(
    private val players: MutableList<BuildPlayer> = mutableListOf(),
    private val teleports: MutableList<TeleportQueue> = mutableListOf()
) {

    init {
        Database.connect("jdbc:h2:./worlds", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Worlds)
        }
    }

    private val worlds: MutableList<BuildWorld> = initializeWorlds()

    val migrateWorlds : List<String>
        get() {
            val worlds =  existingWorlds.filter {
                !it.isWorldRecord() and (it != "world") and (it != "world_nether") and (it != "world_the_end")
            }.toMutableList()

            return worlds
        }


    fun reloadWorlds() = worlds.forEach { world -> world.reload() }

    fun createWorld(name: String, group: String) {
        val record = createWorldRecord(name, group)
        createBuildingWorld(record.id.toString())

        val buildWorld = BuildWorld(record)
        worlds.add(buildWorld)
    }

    fun migrateWorld(world: String, name: String, group: String, state: WorldState) {
        val record = createWorldRecord(name, group, state)

        Bukkit.unloadWorld(world, true)
        val oldFolder = File(Bukkit.getWorldContainer(),world)
        val newFolder = File(Bukkit.getWorldContainer(),record.id.toString())
        oldFolder.renameTo(newFolder)

        val buildWorld = BuildWorld(record)
        buildWorld.load()

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
        return worlds.find { (it.worldIdentifier == world.name) }
    }

    fun asBuildWorld(worldName: String): BuildWorld? {
        return worlds.find { (it.worldIdentifier == worldName) }
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
        val matches = teleports.filter { it.world.worldIdentifier == world.name }
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

    private fun initializeWorlds(): MutableList<BuildWorld> = getWorldRecords().map { BuildWorld(it) }.toMutableList()
       // val list = mutableListOf<BuildWorld>()
       // for (existing in existingWorlds) {
       //   val bukkitWorld = Bukkit.getWorld(existing)
       //   val optional = if (bukkitWorld == null) Optional.empty() else Optional.of(bukkitWorld)
       //   val name = existing
       //   val loadableWorld = LoadableWorld(optional, name)
       //   val buildWorld = BuildWorld(loadableWorld)
       //   list.add(buildWorld)
       // }
       // return list
       // }

}