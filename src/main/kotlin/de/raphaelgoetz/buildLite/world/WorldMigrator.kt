package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.buildLite.sql.isSqlWorld
import org.bukkit.Bukkit
import org.bukkit.generator.ChunkGenerator
import java.io.File
import java.util.UUID

object WorldMigrator {

    /** Returns true only if the legacy folder was found, renamed, and the
     * resulting world was created successfully. Callers must not persist a
     * world record for a migration that returns false. */
    fun migrate(
        oldName: String,
        newUuid: UUID,
        generator: ChunkGenerator,
    ): Boolean {
        val folders = Bukkit.getWorldContainer().listFiles() ?: return false

        for (folder in folders) {
            if (folder.name != oldName) continue

            //Delete level.dat & level_old.dat
            for (file in folder.listFiles() ?: continue) {
                if (file.name == "level.dat" || file.name == "level_old.dat" || file.name == "uid.dat") {
                    file.delete()
                }
            }

            val newFolder = File(folder.parentFile, newUuid.toString())
            if (!folder.renameTo(newFolder)) {
                Bukkit.getLogger().warning("Could not migrate world folder: ${folder.absolutePath} -> ${newFolder.absolutePath}")
                return false
            }

            return try {
                WorldCreator.create(newUuid.toString(), generator)
                true
            } catch (ex: Exception) {
                Bukkit.getLogger().warning("Could not create migrated world '$newUuid': ${ex.message}")
                false
            }
        }

        return false
    }

    fun detect(): List<String> {
        val folders = Bukkit.getWorldContainer().listFiles() ?: return emptyList()
        val result = mutableListOf<String>()

        for (folder in folders) {
            val name = folder.name
            if (name == "world" || name == "world_nether" || name == "world_the_end") continue
            if (name.isSqlWorld()) continue

            for (file in folder.listFiles() ?: continue) {
                if (file.name == "level.dat" || file.name == "level_old.dat") {
                    result.add(folder.name)
                    break
                }
            }
        }

        return result
    }
}
