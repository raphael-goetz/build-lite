package de.raphaelgoetz.buildLite.store

import de.raphaelgoetz.astralis.items.builder.SmartItem
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.astralis.world.existingWorlds
import de.raphaelgoetz.buildLite.record.WorldRecord
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import java.io.File
import java.util.Optional

class BuildWorld(val meta: WorldRecord) {

    val worldIdentifier: String = meta.id.toString()
    val name = meta.name
    val group = meta.group
    val state = meta.state
    var bukkitWorld: Optional<World>
    var hasPhysics: Boolean = false

    val players: List<Player>
        get() {
            if (bukkitWorld.isPresent) {
                return bukkitWorld.get().players.toList()
            }

            return listOf()
        }

    val permissions: List<String>
        get() {
            val root = "betterbuild.enter.*"
            val groupPermission = "betterbuild.enter.$group.*"
            val namePermission = "betterbuild.enter.$group.$name"

            return listOf(root, groupPermission, namePermission)
        }


    init {
        val world = Bukkit.getWorld(name)
        bukkitWorld = if (world == null) {
            Optional.empty()
        } else {
            Optional.of(world)
        }
    }

    fun togglePhysics() {
        hasPhysics = !hasPhysics

        val message = if (hasPhysics) "store.world.pysics.on" else "store.world.pysics.off"
        players.forEach { it.sendTransText(message) {
                type = CommunicationType.UPDATE
            }
        }
    }

    fun isLoaded(): Boolean {
        return bukkitWorld.isPresent
    }

    fun load() {
        val world = Bukkit.getWorld(worldIdentifier)

        if (world == null) {
            Bukkit.createWorld(WorldCreator(worldIdentifier))
        } else {
            this.bukkitWorld = Optional.of(world)
        }
    }

    fun reload() {
        var world = Bukkit.getWorld(worldIdentifier)
        if (world == null) {
            this.bukkitWorld = Optional.empty()
        } else {
            this.bukkitWorld = Optional.of(world)
        }
    }

    fun delete() {
        if (!worldIdentifier.isWorld()) return

        val spawn = Bukkit.getWorld("world") ?: return

        players.forEach { player ->
            player.teleport(spawn.spawnLocation)
        }

        Bukkit.unloadWorld(worldIdentifier, false)
        val folders = Bukkit.getWorldContainer().listFiles() ?: return

        for (folder in folders) {
            if (folder.name != worldIdentifier) continue
            folder.deleteFilesInsideFolder()
        }
    }

    fun asSmartItem(player: Player): SmartItem {
        TODO("")
    }

}

private fun File.deleteFilesInsideFolder() {
    if (!this.isDirectory() && this.delete()) return
    val files = this.listFiles()
    if (files == null || this.delete()) return
    for (content in files) if (!content.delete()) content.deleteFilesInsideFolder()
    if (!this.delete()) this.deleteFilesInsideFolder()
}

fun String.isWorld(): Boolean {
    return existingWorlds.contains(this)
}
