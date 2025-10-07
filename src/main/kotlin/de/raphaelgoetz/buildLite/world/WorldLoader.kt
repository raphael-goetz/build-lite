package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.astralis.event.unregister
import de.raphaelgoetz.astralis.schedule.doLater
import de.raphaelgoetz.buildLite.BuildServer.spawnLocation
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerCredits
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerLocation
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerWarps
import de.raphaelgoetz.buildLite.sql.deleteSqlWorld
import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.toGenerator
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.event.world.WorldLoadEvent
import java.io.File

object WorldLoader {

    fun lazyLoad(loadableWorld: LoadableWorld, generator: WorldGenerator) {
        if (Bukkit.getWorld(loadableWorld.uniqueId.toString()) != null) return
        val creator = WorldCreator(loadableWorld.uniqueId.toString())
        creator.generator(generator.toGenerator())
        creator.createWorld()
    }

    fun lazyTeleport(loadableLocation: LoadableLocation, generator: WorldGenerator, player: Player) {
        val world = Bukkit.getWorld(loadableLocation.worldUuid.toString())
        if (world != null) {
            val location = loadableLocation.toLocation(world)
            player.teleportAsync(location)
            return
        }

        val listener = listen<WorldLoadEvent> { event ->
            if (event.world.name != loadableLocation.worldUuid.toString()) {
                return@listen
            }

            val world = event.world
            val location = loadableLocation.toLocation(world)
            player.teleportAsync(location)
        }

        lazyLoad(LoadableWorld(loadableLocation.worldUuid), generator)

        doLater(2000) {
            listener.unregister()
        }

    }

    fun lazyUnload(world: World) {
        Bukkit.unloadWorld(world, true)
    }

    fun lazyDelete(record: RecordWorld) {

        val nullableWorld = Bukkit.getWorld(record.uniqueId.toString())
        nullableWorld?.let { world ->
            for (player in world.players) {
                player.teleportAsync(spawnLocation)
            }

            Bukkit.unloadWorld(world, false)
        }

        record.deleteSqlPlayerFavorite()
        record.deleteSqlPlayerCredits()
        record.deleteSqlPlayerWarps()
        record.deleteSqlPlayerLocation()
        record.deleteSqlWorld()

        val folders = Bukkit.getWorldContainer().listFiles() ?: return

        for (folder in folders) {
            if (folder.name != record.uniqueId.toString()) continue
            folder.deleteFilesInsideFolder()
        }
    }
}

private fun File.deleteFilesInsideFolder() {
    if (!this.isDirectory() && this.delete()) return
    val files = this.listFiles()
    if (files == null || this.delete()) return
    for (content in files) if (!content.delete()) content.deleteFilesInsideFolder()
    if (!this.delete()) this.deleteFilesInsideFolder()
}