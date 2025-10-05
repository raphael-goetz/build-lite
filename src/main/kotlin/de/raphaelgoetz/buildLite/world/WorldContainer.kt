package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.getAccessibleSqlWorlds
import de.raphaelgoetz.buildLite.sql.getAllSqlWorlds
import de.raphaelgoetz.buildLite.sql.hasSqlPlayerFavorite
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File

object WorldContainer {

    private fun getRegisteredWorlds(): List<RecordWorld> {
        val sqlWorlds = getAllSqlWorlds()
        val acceptableWorlds = mutableSetOf<String>()

        Bukkit.getWorldContainer().listFiles()?.forEach { folder ->
            if (folder.isWorldFolder) {
                acceptableWorlds.add(folder.name)
            }
        }

        return sqlWorlds.filter { it.uniqueId.toString() in acceptableWorlds }
    }

    fun Player.getPermittedWorlds(): List<WorldFolder> {
        val sqlWorlds = getAccessibleSqlWorlds()
        val acceptableWorlds = mutableSetOf<String>()

        Bukkit.getWorldContainer().listFiles()?.forEach { folder ->
            if (folder.isWorldFolder) {
                acceptableWorlds.add(folder.name)
            }
        }

        val worlds = sqlWorlds.filter { it.uniqueId.toString() in acceptableWorlds }
        return worlds
            .groupBy { it.group }
            .map { (group, groupedWorlds) ->
                WorldFolder(group, groupedWorlds)
            }
    }

    fun Player.getPermittedFavoriteWorlds(): List<RecordWorld> {
        val worldFolders = getPermittedWorlds()
        val acceptableWorlds = mutableSetOf<RecordWorld>()

        worldFolders.forEach { folder ->
            folder.worlds.forEach { world ->
                if (hasSqlPlayerFavorite(world.uniqueId)) {
                    acceptableWorlds.add(world)
                }
            }
        }

        return acceptableWorlds.toList()
    }

    val worlds: List<RecordWorld>
        get() = getRegisteredWorlds()

    private val File.isWorldFolder: Boolean
        get() = isDirectory && File(this, ".isWorldFolder").exists()

}