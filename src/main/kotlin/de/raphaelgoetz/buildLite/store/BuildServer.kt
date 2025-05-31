package de.raphaelgoetz.buildLite.store

import de.raphaelgoetz.astralis.world.createBuildingWorld
import de.raphaelgoetz.astralis.world.existingWorlds
import de.raphaelgoetz.buildLite.record.Credits
import de.raphaelgoetz.buildLite.record.WarpRecord
import de.raphaelgoetz.buildLite.record.Warps
import de.raphaelgoetz.buildLite.record.WorldState
import de.raphaelgoetz.buildLite.record.Worlds
import de.raphaelgoetz.buildLite.record.createWorldRecord
import de.raphaelgoetz.buildLite.record.getWorldRecords
import de.raphaelgoetz.buildLite.record.isWorldRecord
import de.raphaelgoetz.buildLite.record.removeAllCredits
import de.raphaelgoetz.buildLite.server.FileServer
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileOutputStream

class BuildServer(
    private val players: MutableList<BuildPlayer> = mutableListOf(),
    private val teleports: MutableList<TeleportQueue> = mutableListOf(),
    private val httpServer: FileServer = FileServer()
) {

    init {
        Database.connect("jdbc:h2:./worlds", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Worlds, Credits, Warps)
        }
    }

    var worlds: MutableList<BuildWorld> = initializeWorlds()

    val migrateWorlds : List<String>
        get() {
            val worlds = existingWorlds.filter {
                println(it)
                !it.isWorldRecord() and (it != "world") and (it != "world_nether") and (it != "world_the_end")
            }.toMutableList()

            return worlds
        }

    fun refetchWorlds() = {
        worlds = initializeWorlds()
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
        world.removeAllCredits()
        world.delete()
    }

    fun asBuildPlayer(player: Player): BuildPlayer? {
        return players.find { (it.player.uniqueId == player.uniqueId) }
    }

    fun asBuildWorld(world: World): BuildWorld? {
        return worlds.find { (it.worldIdentifier == world.name) }
    }

    fun byDisplayIdentifier(identifier: String): BuildWorld? {
        return worlds.find { (it.displayIdentifier == identifier) }
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

    fun queue(buildPlayer: BuildPlayer, world: BuildWorld, warpRecord: WarpRecord? = null) {
        teleports.add(TeleportQueue(buildPlayer, world, warpRecord))
    }

    fun completeQueue(world: World, warp: WarpRecord? = null) {
        val matches = teleports.filter { it.world.worldIdentifier == world.name }
        matches.forEach { teleport ->
            val player = teleport.player.player

            if (warp == null) {
                player.teleport(world.spawnLocation)
            } else {
                val warpLocation = Location(world, warp.x, warp.y, warp.z, warp.yaw, warp.pitch)
                player.teleport(warpLocation)
            }
        }

        teleports.removeAll(matches)
    }

    fun getCategorisedWorlds(): HashMap<String, MutableList<BuildWorld>> {
        val result = HashMap<String, MutableList<BuildWorld>>()

        for (world in worlds) {
            val group = world.group

            if (result.containsKey(group)) {
                val list = result[group] ?: continue
                list += world
                continue
            }

            result.put(group, mutableListOf(world))
        }

        return result
    }

    fun exportWorld(buildWorld: BuildWorld) {
        buildWorld.unload()

        val oldFolder = File(Bukkit.getWorldContainer(),buildWorld.worldIdentifier)
        val result = File(Bukkit.getPluginsFolder(), "BuildLite/export/${buildWorld.group}_${buildWorld.name}.tar.gz")
        createTarGz(oldFolder, result)
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

    fun startServer() {
        httpServer.start()
    }

    fun stopServer() {
        httpServer.stop()
    }

}


fun createTarGz(sourceDir: File, outputFile: File) {
    FileOutputStream(outputFile).use { fos ->
        GzipCompressorOutputStream(fos).use { gcos ->
            TarArchiveOutputStream(gcos).use { tarOut ->
                tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
                addFilesToTar(tarOut, sourceDir, "")
            }
        }
    }
}

private fun addFilesToTar(tarOut: TarArchiveOutputStream, file: File, base: String) {
    val entryName = if (base.isEmpty()) file.name else "$base/${file.name}"

    val entry = TarArchiveEntry(file, entryName)
    tarOut.putArchiveEntry(entry)

    if (file.isFile) {
        file.inputStream().use { input ->
            input.copyTo(tarOut)
        }
        tarOut.closeArchiveEntry()
    } else if (file.isDirectory) {
        tarOut.closeArchiveEntry()
        file.listFiles()?.forEach { child ->
            addFilesToTar(tarOut, child, entryName)
        }
    }
}