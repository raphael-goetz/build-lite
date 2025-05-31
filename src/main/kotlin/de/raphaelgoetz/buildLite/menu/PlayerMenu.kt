package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
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
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.util.function.Consumer

fun BuildPlayer.openPlayerOverviewMenu(server: BuildServer) {
    val player = player
    val players = Bukkit.getOnlinePlayers().map { player ->
        val item = createSmartItem<SkullMeta>(player.name, Material.PLAYER_HEAD, interactionType = InteractionType.SUCCESS) {
            owningPlayer = player
        }
        SmartClick(item, onClick(player.name))
    }

    player.openTransPageInventory(
        "gui.player.title","Players", InventoryRows.ROW6, players, InventorySlots.SLOT1ROW1, InventorySlots.SLOT9ROW5
    ) {
        val left = player.smartTransItem<ItemMeta>("gui.item.arrow.left" , material = Material.ARROW)
        val right = player.smartTransItem<ItemMeta>("gui.item.arrow.right" , material = Material.ARROW)
        pageLeft(InventorySlots.SLOT1ROW6, left.itemStack)
        pageRight(InventorySlots.SLOT9ROW6, right.itemStack)

        val close = player.getItemWithURL(
            Material.BARRIER, DisplayURL.GUI_CLOSE.url, player.locale().getValue("gui.item.main.menu")
        )

        setBlockedSlot(InventorySlots.SLOT5ROW6, close) { event ->
            event.isCancelled = true
            openMainMenu(server)
        }
    }

}

private fun onClick(name: String): Consumer<InventoryClickEvent> {
    return Consumer { event ->
        event.isCancelled = true
        val player = event.whoClicked as Player
        val target = Bukkit.getPlayer(name)

        if (target == null || !target.isOnline) return@Consumer
        player.teleport(target)
    }
}