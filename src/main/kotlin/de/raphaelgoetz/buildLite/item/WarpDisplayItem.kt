package de.raphaelgoetz.buildLite.item

import de.raphaelgoetz.astralis.items.builder.SmartLoreBuilder
import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.buildLite.dialog.warp.showWarpDeletionDialog
import de.raphaelgoetz.buildLite.sql.RecordPlayerWarp
import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.world.LoadableWorld
import de.raphaelgoetz.buildLite.world.WorldContainer.worlds
import de.raphaelgoetz.buildLite.world.WorldLoader
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta

fun Player.createWarpDisplayItem(recordPlayerWarp: RecordPlayerWarp): SmartClick {
    val loadableWorld = LoadableWorld(recordPlayerWarp.worldUuid)
    val material = if (recordPlayerWarp.isPrivate) Material.ENDER_EYE else Material.ENDER_PEARL

    val item = createSmartItem<ItemMeta>(
        name = recordPlayerWarp.name,
        material = material,
        interactionType = InteractionType.CLICK,
    ) {
        val description = getDescription(recordPlayerWarp)
        this.lore(description)
    }

    return SmartClick(item) { click ->
        click.isCancelled = true

        if (click.isShiftClick && click.isRightClick) {
            showWarpDeletionDialog(recordPlayerWarp)
            return@SmartClick
        }

        if (click.isLeftClick) {

            var generator: WorldGenerator? = null
            for (world in worlds) {
                if (recordPlayerWarp.location.worldUuid == world.uniqueId) {
                    generator = world.generator
                    break
                }
            }

            //Only if the match was found. Then the world is probably not existing anymore
            generator?.let {
                closeInventory()
                WorldLoader.lazyTeleport(recordPlayerWarp.location, it, this)
            }

            return@SmartClick
        }

    }
}

private fun getDescription(record: RecordPlayerWarp): List<Component> {

    val first =  if (record.isPrivate)
        "This Warp is private".gray() else
            "This Warp is public".gray()

    val lastSection = mutableListOf(
        "".gray(),
        "Left-Click > Teleport".gray(),
        "Shift + Right-Click > Delete Warp".gray(),
    )
    lastSection.addFirst(first)

    return SmartLoreBuilder(lastSection).build()
}