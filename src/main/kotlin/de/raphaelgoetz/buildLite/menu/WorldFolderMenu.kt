package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.schedule.doNow
import de.raphaelgoetz.astralis.schedule.doNowAsync
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import de.raphaelgoetz.buildLite.item.createInactivePageLeftItem
import de.raphaelgoetz.buildLite.item.createInactivePageRightItem
import de.raphaelgoetz.buildLite.item.createPageLeftItem
import de.raphaelgoetz.buildLite.item.createPageRightItem
import de.raphaelgoetz.buildLite.item.createWorldDisplayItem
import de.raphaelgoetz.buildLite.item.createWorldFolderItem
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.registry.getItemWithURL
import de.raphaelgoetz.buildLite.sql.RecordPlayerCredit
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.getSqlPlayerCreditsFor
import de.raphaelgoetz.buildLite.sql.getSqlBuildTimeByWorld
import de.raphaelgoetz.buildLite.sql.getSqlPlayerFavoriteWorldUuids
import de.raphaelgoetz.buildLite.world.WorldContainer.getPermittedWorlds
import de.raphaelgoetz.buildLite.world.WorldFolder
import org.bukkit.Material
import java.util.UUID

import org.bukkit.entity.Player

fun Player.openWorldFolderMenu() {
    val playerUuid = uniqueId

    doNowAsync {
        // All DB reads happen here, off the main thread. Item/inventory
        // building touches Bukkit APIs, so that stays on the main thread below.
        val permittedWorlds = getPermittedWorlds()
        val worlds = permittedWorlds.flatMap { it.worlds }
        val favoriteUuids = getSqlPlayerFavoriteWorldUuids()
        val favoriteWorlds = worlds.filter { it.uniqueId in favoriteUuids }
        val creditsByWorld = getSqlPlayerCreditsFor(worlds.map { it.uniqueId })
        val buildTimeByWorld = getSqlBuildTimeByWorld(playerUuid)

        doNow {
            if (!isOnline) return@doNow
            renderWorldFolderMenu(
                permittedWorlds,
                favoriteWorlds,
                creditsByWorld,
                favoriteUuids,
                buildTimeByWorld,
            )
        }
    }
}

private fun Player.renderWorldFolderMenu(
    permittedWorlds: List<WorldFolder>,
    favoriteWorlds: List<RecordWorld>,
    creditsByWorld: Map<UUID, List<RecordPlayerCredit>>,
    favoriteUuids: Set<UUID>,
    buildTimeByWorld: Map<UUID, Long>,
) {
    closeDialog()
    val favorites = favoriteWorlds
        .sortedBy { it.name }
        .map {
            createWorldDisplayItem(
                it,
                credits = creditsByWorld[it.uniqueId] ?: emptyList(),
                isFavorite = true,
                buildTimeSeconds = buildTimeByWorld[it.uniqueId] ?: 0L,
            )
        }

    val folders = permittedWorlds
        .sortedBy { it.group }
        .map { folder ->
            createWorldFolderItem(folder) {
                openWorldDisplayMenu(folder, creditsByWorld, favoriteUuids, buildTimeByWorld)
            }
        }

    val clicks = favorites + folders

    openTransPageInventory(
        key = "menu.world_folder.title",
        fallback = "World Folders",
        rows = InventoryRows.ROW6,
        list = clicks,
        from = InventorySlots.SLOT1ROW1,
        to = InventorySlots.SLOT9ROW5,
    )  {
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
