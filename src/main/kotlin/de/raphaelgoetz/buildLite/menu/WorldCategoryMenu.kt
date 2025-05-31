package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.items.builder.SmartLoreBuilder
import de.raphaelgoetz.astralis.items.smartTransItem
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.question.askNewWorldCreate
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.registry.getItemWithURL
import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import de.raphaelgoetz.buildLite.store.BuildWorld
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.meta.ItemMeta
import java.util.function.Consumer

fun BuildPlayer.openWorldCategoryMenu(server: BuildServer) {

    val player = this.player

    val categories = server.getCategorisedWorlds().map { (name, worlds) ->
        worlds.sortBy { it.name }
        val description = getCategoryDescription(worlds)

        val categoryItem = player.getItemWithURL(
            Material.NAME_TAG,
            DisplayURL.ITEM_CATEGORY.url,
            player.locale().getValue("gui.world.item.category.name").replace("category", name),
            description
        )

        SmartClick(categoryItem, onClick(server, this, worlds))
    }

    player.openTransPageInventory(
        "gui.world.category.title",
        "Categories",
        InventoryRows.ROW6,
        categories,
        InventorySlots.SLOT1ROW1,
        InventorySlots.SLOT9ROW5
    ) {
        val left = player.smartTransItem<ItemMeta>("gui.item.arrow.left" , material = Material.ARROW)
        val right = player.smartTransItem<ItemMeta>("gui.item.arrow.right" , material = Material.ARROW)
        pageLeft(InventorySlots.SLOT1ROW6, left.itemStack)
        pageRight(InventorySlots.SLOT9ROW6, right.itemStack)

        val spawn = player.getItemWithURL(
            Material.RESPAWN_ANCHOR, DisplayURL.GUI_SPAWN.url, player.locale().getValue("gui.world.item.spawn.name")
        )

        val create = player.getItemWithURL(
            Material.GRASS_BLOCK, DisplayURL.GUI_WORLD.url, player.locale().getValue("gui.world.item.create.name")
        )

        val close = player.getItemWithURL(
            Material.BARRIER, DisplayURL.GUI_CLOSE.url, player.locale().getValue("gui.item.main.menu")
        )

        setBlockedSlot(InventorySlots.SLOT4ROW6, spawn, onSpawnClick())
        setBlockedSlot(InventorySlots.SLOT5ROW6, create, onCreateWorldClick(server))
        setBlockedSlot(InventorySlots.SLOT6ROW6, close, onCloseClick(this@openWorldCategoryMenu, server))
    }
}

private fun onClick(server: BuildServer, player: BuildPlayer, worlds: List<BuildWorld>): Consumer<InventoryClickEvent> {
    return Consumer { event ->
        player.openWorldListMenu(server, worlds)
    }
}

private fun getCategoryDescription(worlds: MutableList<BuildWorld>): List<Component> {
    val description = adventureText("| Contains: ") {
        type = CommunicationType.NONE
    }

    if (worlds.size > 10) {
        val slice = worlds.slice(IntRange(0, 10))
        val res = slice.map {
            adventureText("| " + it.name) {
                type = CommunicationType.NONE
            }
        }

        val more = adventureText("| and more! ") {
            type = CommunicationType.NONE
        }

        val result = mutableListOf<Component>()
        result.add(description)
        result.addAll(res)
        result.add(more)

        return SmartLoreBuilder(result).build()
    }

    val res = worlds.map {
        adventureText("| " + it.name) {
            type = CommunicationType.NONE
        }
    }

    val result = mutableListOf<Component>()
    result.add(description)
    result.addAll(res)
    return SmartLoreBuilder(result).build()
}

private fun onCloseClick(player: BuildPlayer, buildServer: BuildServer): Consumer<InventoryClickEvent> {
    return Consumer {
        player.openMainMenu(buildServer)
    }
}

private fun onSpawnClick(): Consumer<InventoryClickEvent> {
    return Consumer {
        val world = Bukkit.getWorld("world") ?: return@Consumer
        val player = (it.whoClicked as Player)
        player.teleport(world.spawnLocation)
    }
}

private fun BuildPlayer.onCreateWorldClick(server: BuildServer): Consumer<InventoryClickEvent> {
    return Consumer {
        askNewWorldCreate(server)
    }
}