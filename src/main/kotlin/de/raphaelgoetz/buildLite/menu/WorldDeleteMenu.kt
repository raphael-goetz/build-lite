package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.items.basicSmartTransItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransInventory
import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import de.raphaelgoetz.buildLite.store.BuildWorld
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import java.util.function.Consumer

fun BuildPlayer.openWorldDeleteMenu(server: BuildServer, worlds: BuildWorld) {
    player.openTransInventory(
        "gui.world.delete.title",
        "Delete World",
        InventoryRows.ROW1,
    ) {
        val cancel = player.basicSmartTransItem("gui.world.delete.cancel.name", material = Material.RED_DYE, descriptionKey = "gui.world.delete.cancel.lore", interactionType = InteractionType.DISABLED)
        val confirm = player.basicSmartTransItem("gui.world.delete.confirm.name", material = Material.LIME_DYE, descriptionKey = "gui.world.delete.confirm.lore", interactionType = InteractionType.ENABLED)

        setBlockedSlot(InventorySlots.SLOT4ROW1, cancel, onCancelClick())
        setBlockedSlot(InventorySlots.SLOT6ROW1, confirm, onConfirmClick(server, worlds))
    }
}

private fun onConfirmClick(server: BuildServer, world: BuildWorld): Consumer<InventoryClickEvent> {
    return Consumer { event ->
        event.whoClicked.closeInventory()
        server.deleteWorld(world)
    }
}

private fun onCancelClick(): Consumer<InventoryClickEvent> {
    return Consumer { event ->
        event.whoClicked.closeInventory()
    }
}

