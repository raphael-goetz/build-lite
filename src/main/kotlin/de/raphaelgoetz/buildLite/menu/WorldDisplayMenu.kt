package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.schedule.doNow
import de.raphaelgoetz.astralis.schedule.doNowAsync
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.item.createInactivePageLeftItem
import de.raphaelgoetz.buildLite.item.createInactivePageRightItem
import de.raphaelgoetz.buildLite.item.createPageLeftItem
import de.raphaelgoetz.buildLite.item.createPageRightItem
import de.raphaelgoetz.buildLite.item.createWorldDisplayItem
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.registry.getItemWithURL
import de.raphaelgoetz.buildLite.sql.RecordPlayerCredit
import de.raphaelgoetz.buildLite.sql.getSqlPlayerCreditsFor
import de.raphaelgoetz.buildLite.sql.getSqlBuildTimeByWorld
import de.raphaelgoetz.buildLite.sql.getSqlPlayerFavoriteWorldUuids
import de.raphaelgoetz.buildLite.world.WorldFolder
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID

fun Player.openWorldDisplayMenu(folder: WorldFolder) {
    closeDialog()
    val playerUuid = uniqueId
    sendActionBar(net.kyori.adventure.text.Component.text("Loading worlds…"))

    doNowAsync {
        val creditsByWorld = getSqlPlayerCreditsFor(folder.worlds.map { it.uniqueId })
        val favoriteUuids = getSqlPlayerFavoriteWorldUuids()
        val buildTimeByWorld = getSqlBuildTimeByWorld(playerUuid)

        doNow {
            if (isOnline) renderWorldDisplayMenu(folder, creditsByWorld, favoriteUuids, buildTimeByWorld)
        }
    }
}

/** Opens a folder from a snapshot already loaded for the parent menu. */
fun Player.openWorldDisplayMenu(
    folder: WorldFolder,
    creditsByWorld: Map<UUID, List<RecordPlayerCredit>>,
    favoriteUuids: Set<UUID>,
    buildTimeByWorld: Map<UUID, Long>,
) {
    renderWorldDisplayMenu(folder, creditsByWorld, favoriteUuids, buildTimeByWorld)
}

private fun Player.renderWorldDisplayMenu(
    folder: WorldFolder,
    creditsByWorld: Map<UUID, List<RecordPlayerCredit>>,
    favoriteUuids: Set<UUID>,
    buildTimeByWorld: Map<UUID, Long>,
) {
    val worlds = folder.worlds
        .sortedBy { it.name }
        .map {
            createWorldDisplayItem(
                it,
                credits = creditsByWorld[it.uniqueId] ?: emptyList(),
                isFavorite = it.uniqueId in favoriteUuids,
                buildTimeSeconds = buildTimeByWorld[it.uniqueId] ?: 0L,
            )
        }

    openTransPageInventory(
        key = "menu.world_display.title",
        fallback = "Worlds",
        rows = InventoryRows.ROW6,
        list = worlds,
        from = InventorySlots.SLOT1ROW1,
        to = InventorySlots.SLOT9ROW5,
    ) {
        pageLeft(InventorySlots.SLOT1ROW6, createPageLeftItem(), createInactivePageLeftItem())
        pageRight(InventorySlots.SLOT9ROW6, createPageRightItem(), createInactivePageRightItem())

        val close = getItemWithURL(
            Material.BARRIER,
            DisplayURL.GUI_CLOSE.url,
            locale().getValue("gui.item.main.menu")
        )

        setBlockedSlot(InventorySlots.SLOT5ROW6, close) { event ->
            event.isCancelled = true
            openWorldFolderMenu()
        }
    }
}
