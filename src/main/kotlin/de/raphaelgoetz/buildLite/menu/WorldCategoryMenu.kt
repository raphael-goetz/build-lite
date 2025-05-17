package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.items.basicItemWithoutMeta
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
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
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
        val left = basicItemWithoutMeta(Material.ARROW)
        val right = basicItemWithoutMeta(Material.ARROW)
        pageLeft(InventorySlots.SLOT1ROW6, left)
        pageRight(InventorySlots.SLOT9ROW6, right)

        val spawn = player.getItemWithURL(
            Material.RESPAWN_ANCHOR, DisplayURL.GUI_SPAWN.url, player.locale().getValue("gui.world.item.spawn.name")
        )

        val create = player.getItemWithURL(
            Material.GRASS_BLOCK, DisplayURL.GUI_WORLD.url, player.locale().getValue("gui.world.item.create.name")
        )

        val close = player.getItemWithURL(
            Material.BARRIER, DisplayURL.GUI_CLOSE.url, player.locale().getValue("gui.world.item.close.name")
        )

        setBlockedSlot(InventorySlots.SLOT4ROW6, spawn, onSpawnClick())
        setBlockedSlot(InventorySlots.SLOT5ROW6, create, onCreateWorldClick(server))
        setBlockedSlot(InventorySlots.SLOT6ROW6, close, onCloseClick())
    }
}

private fun onClick(server: BuildServer, player: BuildPlayer, worlds: List<BuildWorld>): Consumer<InventoryClickEvent> {
    return Consumer { event ->
        player.openWorldListMenu(server, worlds)
    }
}

private fun getCategoryDescription(worlds: MutableList<BuildWorld>): String {
    val description = StringBuilder()
    description.append("Contains: ")
    for (i in worlds.indices) description.append(worlds[i].name + ", ")
    return description.toString()
}

private fun onCloseClick(): Consumer<InventoryClickEvent> {
    return Consumer {
        val player = (it.whoClicked as Player)
        player.closeInventory()
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