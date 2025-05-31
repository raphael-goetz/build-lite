package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.items.smartTransItem
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.record.WarpRecord
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.registry.getItemWithURL
import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.meta.ItemMeta
import java.util.function.Consumer
import kotlin.jvm.optionals.getOrElse

fun BuildPlayer.openWarpMenu(warps: List<WarpRecord>, server: BuildServer) {
    val player = this.player

    val warpClick = warps.map { warp ->
        val item = createSmartItem<ItemMeta>(warp.name, material = Material.PAPER, interactionType = InteractionType.SUCCESS)
        SmartClick(item, onClick(server, warp))
    }

    player.openTransPageInventory(
        "gui.world.warps.title",
        "Warps",
        InventoryRows.ROW6,
        warpClick,
        InventorySlots.SLOT1ROW1,
        InventorySlots.SLOT9ROW5
    ) {
        val left = player.smartTransItem<ItemMeta>("gui.item.arrow.left" , material = Material.ARROW)
        val right = player.smartTransItem<ItemMeta>("gui.item.arrow.right" , material = Material.ARROW)
        pageLeft(InventorySlots.SLOT1ROW6, left.itemStack)
        pageRight(InventorySlots.SLOT9ROW6, right.itemStack)

        val back = this@openWarpMenu.player.getItemWithURL(
            Material.STRUCTURE_VOID, DisplayURL.GUI_BACK.url, player.locale().getValue("gui.world.item.back.name"), interactionType = InteractionType.SUCCESS
        )

        setBlockedSlot(InventorySlots.SLOT5ROW6, back) {
            this@openWarpMenu.openWorldCategoryMenu(server)
        }
    }
}

private fun BuildPlayer.onClick(server: BuildServer, warpRecord: WarpRecord): Consumer<InventoryClickEvent> {
    return Consumer { event ->
        event.isCancelled = true
        player.closeInventory()

        val world = server.asBuildWorld(warpRecord.worldUUID.toString()) ?: return@Consumer

        if (world.isLoaded()) {
            val bukkitWorld = world.bukkitWorld.getOrElse {
                return@Consumer
            }

            val loc = Location(bukkitWorld, warpRecord.x, warpRecord.y, warpRecord.z, warpRecord.yaw, warpRecord.pitch)
            player.teleport(loc)
            return@Consumer
        }

        server.queue(this, world, warpRecord)
        world.load()
    }
}