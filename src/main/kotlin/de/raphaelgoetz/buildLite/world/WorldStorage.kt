package de.raphaelgoetz.buildLite.world

import org.bukkit.Bukkit
import java.io.File

/**
 * Paper 26.2 introduced a WorldFolderMigration that nests every non-default
 * world under `<primary-world>/dimensions/minecraft/<name>` instead of giving
 * it its own top-level folder in the world container. Custom worlds created
 * by this plugin get migrated into that layout the moment they're loaded, so
 * lookups need to check both locations to keep working across the migration.
 */
object WorldStorage {

    private fun primaryWorldName(): String = Bukkit.getWorlds().firstOrNull()?.name ?: "world"

    private fun dimensionsDir(): File =
        File(File(Bukkit.getWorldContainer(), primaryWorldName()), "dimensions/minecraft")

    fun existingWorldFolderNames(): Set<String> {
        val topLevel = Bukkit.getWorldContainer().listFiles()?.mapTo(mutableSetOf()) { it.name } ?: mutableSetOf()
        val nested = dimensionsDir().listFiles()?.mapTo(mutableSetOf()) { it.name } ?: emptySet()
        return topLevel + nested
    }

    fun folderFor(name: String): File? {
        val flat = File(Bukkit.getWorldContainer(), name)
        if (flat.exists()) return flat

        val nested = File(dimensionsDir(), name)
        if (nested.exists()) return nested

        return null
    }
}
