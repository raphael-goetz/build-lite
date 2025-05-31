package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.items.smartTransItem
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.record.getCredits
import de.raphaelgoetz.buildLite.store.BuildWorld
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta

fun Player.openCreditMenu(world: BuildWorld) {

    val credits = world.getCredits().map {
        val offlinePlayer = Bukkit.getOfflinePlayer(it.playerUUID)
        var profile = offlinePlayer.playerProfile

        if (profile.textures.isEmpty) {
           profile = profile.update().join()
        }

        val head = createSmartItem<SkullMeta>(profile.name!!, Material.PLAYER_HEAD, interactionType = InteractionType.CLICK) {
            owningPlayer = offlinePlayer
            playerProfile = profile
        }

        SmartClick(head) { event ->
            event.isCancelled = true
        }
    }

    openTransPageInventory("gui.credits.title", "Credits", InventoryRows.ROW6, credits, InventorySlots.SLOT1ROW1, InventorySlots.SLOT9ROW5) {
        val left = smartTransItem<ItemMeta>("gui.item.arrow.left" , material = Material.ARROW)
        val right = smartTransItem<ItemMeta>("gui.item.arrow.right" , material = Material.ARROW)
        pageLeft(InventorySlots.SLOT1ROW6, left.itemStack)
        pageRight(InventorySlots.SLOT9ROW6, right.itemStack)
    }

}
