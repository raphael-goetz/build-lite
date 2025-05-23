package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.items.builder.SmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.items.smartItemWithoutMeta
import de.raphaelgoetz.astralis.items.smartTransItem
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.registry.getItemWithURL
import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import de.raphaelgoetz.buildLite.store.BuildWorld
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.meta.ItemMeta
import java.util.function.Consumer
import kotlin.jvm.optionals.getOrElse

fun BuildPlayer.openWorldListMenu(server: BuildServer, worlds: List<BuildWorld>) {

    val player = this.player

    val worldClick = worlds.map {
        val item = getWorldItem(it)
        SmartClick(item, onClick(server, it))
    }

    player.openTransPageInventory(
        "gui.world.list.title",
        "Worlds",
        InventoryRows.ROW6,
        worldClick,
        InventorySlots.SLOT1ROW1,
        InventorySlots.SLOT9ROW5
    ) {
        val left = player.smartTransItem<ItemMeta>("gui.item.arrow.left" , material = Material.ARROW)
        val right = player.smartTransItem<ItemMeta>("gui.item.arrow.right" , material = Material.ARROW)
        pageLeft(InventorySlots.SLOT1ROW6, left.itemStack)
        pageRight(InventorySlots.SLOT9ROW6, right.itemStack)

        val back = this@openWorldListMenu.player.getItemWithURL(
            Material.STRUCTURE_VOID, DisplayURL.GUI_BACK.url, player.locale().getValue("gui.world.item.back.name"), interactionType = InteractionType.SUCCESS
        )

        setBlockedSlot(InventorySlots.SLOT5ROW6, back) {
           this@openWorldListMenu.openWorldCategoryMenu(server)
        }
    }
}

private fun BuildPlayer.onClick(server: BuildServer, world: BuildWorld): Consumer<InventoryClickEvent> {
    return Consumer { event ->
        event.isCancelled = true
        player.closeInventory()

        if (world.isLoaded()) {
            val loc = world.bukkitWorld.getOrElse {
                return@Consumer
            }

            player.teleport(loc.spawnLocation)
            return@Consumer
        }

        server.queue(this, world)
        world.load()
    }
}

private fun BuildPlayer.getWorldItem(world: BuildWorld): SmartItem {
    return this.player.getItemWithURL(
        Material.GRASS_BLOCK,
        DisplayURL.ITEM_WORLD_1.url,
        player.locale().getValue("gui.world.item.world.name").replace("world", world.name),
    )
}