package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import de.raphaelgoetz.buildLite.item.createInactivePageLeftItem
import de.raphaelgoetz.buildLite.item.createInactivePageRightItem
import de.raphaelgoetz.buildLite.item.createPageLeftItem
import de.raphaelgoetz.buildLite.item.createPageRightItem
import de.raphaelgoetz.buildLite.item.createReviewDisplayItem
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.registry.getItemWithURL
import de.raphaelgoetz.buildLite.sql.getSqlPlayerReviews
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID

fun Player.openReviewMenu(worldUUID: UUID) {
    closeDialog()
    val clicks = getSqlPlayerReviews(worldUUID).map {
        createReviewDisplayItem(it)
    }

    openTransPageInventory(
        key = "",
        fallback = "Reviews",
        rows = InventoryRows.ROW6,
        list = clicks,
        from = InventorySlots.SLOT1ROW1,
        to = InventorySlots.SLOT9ROW5
    ) {
        pageLeft(InventorySlots.SLOT1ROW6, createPageLeftItem(), createInactivePageLeftItem())
        pageRight(InventorySlots.SLOT9ROW6, createPageRightItem(), createInactivePageRightItem())

        val close = getItemWithURL(
            Material.BARRIER,
            DisplayURL.GUI_CLOSE.url,
            locale().getValue("gui.item.back.menu")
        )

        setBlockedSlot(InventorySlots.SLOT5ROW6, close) { event ->
            event.isCancelled = true
            openWorldFolderMenu()
        }
    }
}
