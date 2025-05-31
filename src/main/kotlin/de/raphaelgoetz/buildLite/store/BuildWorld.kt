package de.raphaelgoetz.buildLite.store

import de.raphaelgoetz.astralis.items.builder.SmartItem
import de.raphaelgoetz.astralis.items.builder.SmartLoreBuilder
import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.astralis.world.existingWorlds
import de.raphaelgoetz.buildLite.record.WorldRecord
import de.raphaelgoetz.buildLite.record.WorldState
import de.raphaelgoetz.buildLite.record.updateGroup
import de.raphaelgoetz.buildLite.record.updateName
import de.raphaelgoetz.buildLite.record.updateSpawnLocation
import de.raphaelgoetz.buildLite.record.updateStatus
import de.raphaelgoetz.buildLite.registry.DisplayURL
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import java.io.File
import java.net.URI
import java.util.Optional
import java.util.UUID

class BuildWorld(val meta: WorldRecord) {

    var name = meta.name
    var group = meta.group
    val worldIdentifier: String = meta.id.toString()
    val displayIdentifier = group + "_" + name
    var state = meta.state
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
        val world = Bukkit.getWorld(worldIdentifier)
        if (world == null) {
            this.bukkitWorld = Optional.empty()
        } else {
            this.bukkitWorld = Optional.of(world)
        }
    }

    fun delete() {
        unload()
        val folders = Bukkit.getWorldContainer().listFiles() ?: return

        for (folder in folders) {
            if (folder.name != worldIdentifier) continue
            folder.deleteFilesInsideFolder()
        }
    }

    fun unload() {
        if (!worldIdentifier.isWorld()) return

        val spawn = Bukkit.getWorld("world") ?: return

        players.forEach { player ->
            player.teleport(spawn.spawnLocation)
        }

        Bukkit.unloadWorld(worldIdentifier, false)
    }

    fun updateSpawn(location: Location) {
        meta.updateSpawnLocation(location)
        this.bukkitWorld.ifPresent { world -> world.spawnLocation = location }
    }

    fun updateState(state: WorldState) {
        this.state = state
        this.meta.updateStatus(state)
    }

    fun updateGroup(group: String) {
        this.group = group
        this.meta.updateGroup(group)
    }

    fun updateName(name: String) {
        this.name = name
        this.meta.updateName(name)
    }

    fun asSmartItem(player: Player): SmartItem {
        val name = adventureText("| Name: $name") {
            type = CommunicationType.NONE
            italic(false)
        }

        val group = adventureText("| Group: $group") {
            type = CommunicationType.NONE
            italic(false)
        }

        val state = adventureText("| State: $state") {
            type = CommunicationType.NONE
            italic(false)
        }

        val description = SmartLoreBuilder(mutableListOf(name, group, state)).build()
        val itemName = player.locale().getValue("gui.world.item.world.name").replace("world", this.name)

        return createSmartItem<SkullMeta>(itemName, material = Material.PLAYER_HEAD, interactionType = InteractionType.DISPLAY_CLICK) {
            this.lore(description)

            val newPlayerProfile = Bukkit.createProfile(UUID.randomUUID())
            val playerTextures = newPlayerProfile.textures

            playerTextures.skin = URI(DisplayURL.ITEM_WORLD_1.url).toURL()
            newPlayerProfile.setTextures(playerTextures)

            playerProfile = newPlayerProfile
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

fun String.isWorld(): Boolean {
    return existingWorlds.contains(this)
}