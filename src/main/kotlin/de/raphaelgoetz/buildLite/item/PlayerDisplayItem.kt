package de.raphaelgoetz.buildLite.item

import de.raphaelgoetz.astralis.items.builder.SmartLoreBuilder
import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta

fun Player.createPlayerDisplayItem(targetPlayer: Player): SmartClick {

    val item = createSmartItem<SkullMeta>(
        name = targetPlayer.name,
        material = Material.PLAYER_HEAD,
        interactionType = InteractionType.DISPLAY_CLICK,
    ) {
        owningPlayer = targetPlayer
        val description = getDescription()
        this.lore(description)
    }

    return SmartClick(item) { event ->
        event.isCancelled = true

        if (event.isLeftClick) {
            teleport(targetPlayer.location)
            return@SmartClick
        }

        if (event.isRightClick) {
            teleport(targetPlayer.location)
            gameMode = GameMode.SPECTATOR
            spectatorTarget = targetPlayer

            return@SmartClick
        }
    }

}

private fun getDescription(): List<Component> {
    val lastSection = mutableListOf(
        "".gray(),
        "Left-Click > Teleport".gray(),
        "Right-Click > Spectate".gray(),
    )

    return SmartLoreBuilder(lastSection).build()
}