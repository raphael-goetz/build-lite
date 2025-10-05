package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.item.createPageLeftItem
import de.raphaelgoetz.buildLite.item.createPageRightItem
import de.raphaelgoetz.buildLite.item.createWorldDisplayItem
import de.raphaelgoetz.buildLite.world.WorldFolder
import org.bukkit.entity.Player

fun Player.openWorldDisplayMenu(folder: WorldFolder) {
    val worlds = folder.worlds.map { createWorldDisplayItem(it) }

    openTransPageInventory(
        key = "menu.world_display.title",
        fallback = "Worlds",
        rows = InventoryRows.ROW6,
        list = worlds,
        from = InventorySlots.SLOT1ROW1,
        to = InventorySlots.SLOT9ROW5,
    ) {
        pageLeft(InventorySlots.SLOT1ROW6, createPageLeftItem())
        pageRight(InventorySlots.SLOT9ROW6, createPageRightItem())
    }
}