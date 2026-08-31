package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.schedule.doNow
import de.raphaelgoetz.astralis.schedule.doNowAsync
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import de.raphaelgoetz.buildLite.cache.PlayerProfileCache
import de.raphaelgoetz.buildLite.item.createBuildTimePlayerPickerItem
import de.raphaelgoetz.buildLite.item.createInactivePageLeftItem
import de.raphaelgoetz.buildLite.item.createInactivePageRightItem
import de.raphaelgoetz.buildLite.item.createPageLeftItem
import de.raphaelgoetz.buildLite.item.createPageRightItem
import de.raphaelgoetz.buildLite.item.createWorldBuildTimeDisplayItem
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.registry.getItemWithURL
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.getAllSqlWorlds
import de.raphaelgoetz.buildLite.sql.getSqlBuildTimeByWorld
import de.raphaelgoetz.buildLite.sql.selectUniquePlayerUuids
import org.bukkit.Material
import org.bukkit.entity.Player
import java.time.LocalDate
import java.util.UUID

fun Player.openBuildTimeMenu(targetUuid: UUID, targetName: String, startDate: LocalDate, endDate: LocalDate) {
    doNowAsync {
        val timeByWorld = getSqlBuildTimeByWorld(targetUuid, startDate, endDate)
        val worlds = getAllSqlWorlds().filter { timeByWorld.containsKey(it.uniqueId) }

        doNow {
            if (isOnline) renderBuildTimeMenu(targetName, startDate, endDate, worlds, timeByWorld)
        }
    }
}

private fun Player.renderBuildTimeMenu(
    targetName: String,
    startDate: LocalDate,
    endDate: LocalDate,
    worlds: List<RecordWorld>,
    timeByWorld: Map<UUID, Long>,
) {
    closeDialog()
    val items = worlds
        .sortedByDescending { timeByWorld[it.uniqueId] ?: 0L }
        .map { createWorldBuildTimeDisplayItem(it, timeByWorld[it.uniqueId] ?: 0L) }

    openTransPageInventory(
        key = "menu.build_time.title",
        fallback = "$targetName - Build Time ($startDate - $endDate)",
        rows = InventoryRows.ROW6,
        list = items,
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
            showHomeDialog()
        }
    }
}

fun Player.openBuildTimePlayerPickerMenu() {
    doNowAsync {
        val playerUuids = selectUniquePlayerUuids()

        doNow {
            if (!isOnline) return@doNow
            renderBuildTimePlayerPickerMenu(playerUuids)
        }
    }
}

private fun Player.renderBuildTimePlayerPickerMenu(playerUuids: List<UUID>) {
    closeDialog()
    val clicks = playerUuids
        .map { PlayerProfileCache.getOrFetch(it) }
        .sortedBy { it.playerName.lowercase() }
        .map { createBuildTimePlayerPickerItem(it) }

    openTransPageInventory(
        key = "menu.build_time_picker.title",
        fallback = "Build Time - Select Player",
        rows = InventoryRows.ROW6,
        list = clicks,
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

        setBlockedSlot(InventorySlots.SLOT5ROW6, close) { event -> event.isCancelled = true }
    }
}
