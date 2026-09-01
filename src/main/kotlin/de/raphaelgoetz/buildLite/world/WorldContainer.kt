package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.getAccessibleSqlWorlds
import de.raphaelgoetz.buildLite.sql.getAllSqlWorlds
import de.raphaelgoetz.buildLite.sql.getSqlPlayerFavoriteWorldUuids
import org.bukkit.entity.Player

object WorldContainer {

    private fun getRegisteredWorlds(): List<RecordWorld> {
        val folderNames = WorldStorage.existingWorldFolderNames()
        return getAllSqlWorlds().filter { it.uniqueId.toString() in folderNames }
    }

    fun Player.getPermittedWorlds(): List<WorldFolder> {
        val folderNames = WorldStorage.existingWorldFolderNames()
        val worlds = getAccessibleSqlWorlds().filter { it.uniqueId.toString() in folderNames }

        return worlds
            .groupBy { it.group }
            .map { (group, groupedWorlds) ->
                WorldFolder(group, groupedWorlds)
            }
    }

    fun Player.getPermittedFavoriteWorlds(worldFolders: List<WorldFolder> = getPermittedWorlds()): List<RecordWorld> {
        val favoriteUuids = getSqlPlayerFavoriteWorldUuids()

        return worldFolders
            .flatMap { it.worlds }
            .filter { it.uniqueId in favoriteUuids }
            .distinct()
    }

    val worlds: List<RecordWorld>
        get() = getRegisteredWorlds()

}