package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.item.createInactivePageLeftItem
import de.raphaelgoetz.buildLite.item.createInactivePageRightItem
import de.raphaelgoetz.buildLite.item.createPageLeftItem
import de.raphaelgoetz.buildLite.item.createPageRightItem
import de.raphaelgoetz.buildLite.item.createWorldMigrationItem
import de.raphaelgoetz.buildLite.world.WorldMigrator
import org.bukkit.entity.Player

fun Player.openWorldMigrationMenu() {
    val clicks = WorldMigrator.detect().map { createWorldMigrationItem(it) }

    openTransPageInventory(
        key = "menu.world_migration.title",
        fallback = "Migrate Worlds",
        rows = InventoryRows.ROW6,
        list = clicks,
        from = InventorySlots.SLOT1ROW1,
        to = InventorySlots.SLOT9ROW5,
    )  {
        pageLeft(InventorySlots.SLOT1ROW6, createPageLeftItem(), createInactivePageLeftItem())
        pageRight(InventorySlots.SLOT9ROW6, createPageRightItem(), createInactivePageRightItem())
    }
}