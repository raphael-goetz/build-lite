package de.raphaelgoetz.buildLite.item

import de.raphaelgoetz.astralis.items.builder.SmartLoreBuilder
import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.buildLite.dialog.world.showWorldMigrationDialog
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta

fun Player.createWorldMigrationItem(worldName: String): SmartClick {
    val item = createSmartItem<ItemMeta>(
        name = worldName,
        material = Material.CHEST,
        interactionType = InteractionType.CLICK,
    ) {
        val description = getDescription()
        this.lore(description)
    }

    return SmartClick(item) { click ->
        click.isCancelled = true
        showWorldMigrationDialog(worldName)
    }
}

private fun getDescription(): List<Component> {
    val lastSection = mutableListOf(
        "".gray(),
        "Left-Click > Start Migration Process".gray(),
    )
    return SmartLoreBuilder(lastSection).build()
}