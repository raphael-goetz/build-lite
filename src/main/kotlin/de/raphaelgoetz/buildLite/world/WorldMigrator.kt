package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.buildLite.sql.isSqlWorld
import org.bukkit.Bukkit
import org.bukkit.generator.ChunkGenerator
import java.io.File
import java.util.UUID

object WorldMigrator {

    fun migrate(
        oldName: String,
        newUuid: UUID,
        generator: ChunkGenerator,
    ) {
        val folders = Bukkit.getWorldContainer().listFiles() ?: return

        for (folder in folders) {
            if (folder.name != oldName) continue

            //Delete level.dat & level_old.dat
            for (file in folder.listFiles() ?: continue) {
                if (file.name == "level.dat" || file.name == "level_old.dat" || file.name == "uid.dat") {
                    file.delete()
                }
            }

            val newFolder = File(folder.parentFile, newUuid.toString())
            folder.renameTo(newFolder)

            WorldCreator.create(newUuid.toString(), generator)
        }
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
