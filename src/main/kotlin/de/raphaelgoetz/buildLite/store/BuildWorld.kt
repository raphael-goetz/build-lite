package de.raphaelgoetz.buildLite.store


import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.astralis.world.existingWorlds
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File

class BuildWorld(val loadableWorld: LoadableWorld, var hasPhysics: Boolean = false) {

    private val worldName: String = loadableWorld.name

    fun reload() = loadableWorld.reload()

    fun getWorldName(): String = loadableWorld.name

    fun togglePhysics() {
        System.out.println("Before toggle: $hasPhysics")
        hasPhysics = !hasPhysics
        System.out.println("After toggle: $hasPhysics")

        val message = if (hasPhysics) "store.world.pysics.on" else "store.world.pysics.off"
        players().forEach { it.sendTransText(message) {
                type = CommunicationType.UPDATE
            }
        }
    }

    val name = if (loadableWorld.name.contains("_")) loadableWorld.name.substring(loadableWorld.name.indexOf("_") + 1) else loadableWorld.name
    val group = if (loadableWorld.name.contains("_")) loadableWorld.name.substring(0, loadableWorld.name.indexOf("_")) else "unknown"

    val permissions: List<String>
        get() {
            val root = "betterbuild.enter.*"
            val group = "betterbuild.enter.$group.*"
            val name = "betterbuild.enter.$group.$name"

            return listOf(root, group, name)
        }

    fun isLoaded(): Boolean {
        return loadableWorld.optionalWorld.isPresent
    }

    fun players(): List<Player> {
        return loadableWorld.getPlayers()
    }

    fun delete() {
        if (!worldName.isWorld()) return

        val spawn = Bukkit.getWorld("world") ?: return

        players().forEach { player ->
            player.teleport(spawn.spawnLocation)
        }

        Bukkit.unloadWorld(worldName, false)
        val folders = Bukkit.getWorldContainer().listFiles() ?: return

        for (folder in folders) {
            if (folder.name != worldName) continue
            folder.deleteFilesInsideFolder()
        }
    }

    private fun File.deleteFilesInsideFolder() {
        if (!this.isDirectory() && this.delete()) return
        val files = this.listFiles()
        if (files == null || this.delete()) return
        for (content in files) if (!content.delete()) content.deleteFilesInsideFolder()
        if (!this.delete()) this.deleteFilesInsideFolder()
    }
}

fun String.isWorld(): Boolean {
    return existingWorlds.contains(this)
}
