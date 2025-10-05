package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.item.createPageLeftItem
import de.raphaelgoetz.buildLite.item.createPageRightItem
import de.raphaelgoetz.buildLite.item.createWorldDisplayItem
import de.raphaelgoetz.buildLite.item.createWorldFolderItem
import de.raphaelgoetz.buildLite.world.WorldContainer.getPermittedFavoriteWorlds
import de.raphaelgoetz.buildLite.world.WorldContainer.getPermittedWorlds
import org.bukkit.entity.Player

fun Player.openWorldFolderMenu() {
    val favorites = getPermittedFavoriteWorlds().map { createWorldDisplayItem(it) }
    val folders = getPermittedWorlds().map { createWorldFolderItem(it) }
    val clicks = favorites + folders

    openTransPageInventory(
        key = "menu.world_folder.title",
        fallback = "World Folders",
        rows = InventoryRows.ROW6,
        list = clicks,
        from = InventorySlots.SLOT1ROW1,
        to = InventorySlots.SLOT9ROW5,
    )  {
        pageLeft(InventorySlots.SLOT1ROW6, createPageLeftItem())
        pageRight(InventorySlots.SLOT9ROW6, createPageRightItem())
    }
}