package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.astralis.event.unregister
import de.raphaelgoetz.astralis.schedule.doLater
import de.raphaelgoetz.buildLite.cache.CacheReview
import de.raphaelgoetz.buildLite.config.toJson
import de.raphaelgoetz.buildLite.config.toMeta
import de.raphaelgoetz.buildLite.spawnLocation
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerCredits
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerLocation
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerWarps
import de.raphaelgoetz.buildLite.sql.deleteSqlWorld
import de.raphaelgoetz.buildLite.sql.getSqlPlayerReviews
import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.toGenerator

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

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

            CacheReview.loadWorld(event.world, getSqlPlayerReviews(loadableLocation.worldUuid))
            val world = event.world
            val location = loadableLocation.toLocation(world)
            player.teleportAsync(location)
        }

        lazyLoad(LoadableWorld(loadableLocation.worldUuid), generator)

        doLater(5) {
            listener.unregister()
        }
    }

    fun lazyUnload(world: World) {
        for (player in world.players) {
            player.teleportAsync(spawnLocation)
        }

        CacheReview.unloadWorld(world)
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

    fun lazyExport(record: RecordWorld) {
        val world = Bukkit.getWorlds().firstOrNull { it.name == record.uniqueId.toString() }

        if (world != null) {
            lazyUnload(world)

            val listener = listen<WorldUnloadEvent> { event ->
                if (event.world.name == record.uniqueId.toString()) {
                    exportWorldFolder(record)
                }
            }

            doLater(5) { listener.unregister() }
        } else {
            // World not loaded, export immediately
            exportWorldFolder(record)
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

fun createTarGz(sourceDir: File, outputFile: File) {
    FileOutputStream(outputFile).use { fos ->
        GzipCompressorOutputStream(fos).use { gzos ->
            TarArchiveOutputStream(gzos).use { tarOut ->
                tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
                addFilesToTarGzSafe(sourceDir, sourceDir, tarOut)
            }
        }
    }
}

fun addFilesToTarGzSafe(root: File, source: File, tarOut: TarArchiveOutputStream) {
    if (source.isFile) {
        // Skip session.lock and any other locked/system files
        if (source.name.equals("session.lock", ignoreCase = true)) {
            println("Skipping locked file: ${source.absolutePath}")
            return
        }

        if (source.name.equals(".isWorldFolder", ignoreCase = true)) {
            println("Skipping identifier file: ${source.absolutePath}")
            return
        }

        val relativePath = source.relativeTo(root).path.replace("\\", "/")
        val entry = TarArchiveEntry(source, relativePath)
        tarOut.putArchiveEntry(entry)


        try {
            FileInputStream(source).use { fis ->
                fis.copyTo(tarOut)
            }

        } catch (ex: IOException) {
            Bukkit.getLogger().warning("Could not copy file: ${source.absolutePath}, ${ex.message}")
        }

        tarOut.closeArchiveEntry()
    } else if (source.isDirectory) {
        source.listFiles()?.forEach { addFilesToTarGzSafe(root, it, tarOut) }
    }
}


private fun exportWorldFolder(record: RecordWorld) {
    val worldFolder = File(Bukkit.getWorldContainer(), record.uniqueId.toString())
    if (!worldFolder.exists()) return

    // Ensure export folder exists
    val result = File(Bukkit.getPluginsFolder(), "build-lite/export/${record.uniqueId}.tar.gz")
    result.parentFile.mkdirs()
    if (result.exists()) result.delete()

    // Write meta.json into the world folder so it gets included in the zip
    val metaFile = File(worldFolder, "meta.json")
    metaFile.writeText(record.toMeta().toJson())

    createTarGz(worldFolder, result)
}