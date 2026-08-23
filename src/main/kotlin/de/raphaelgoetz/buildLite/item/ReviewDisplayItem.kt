package de.raphaelgoetz.buildLite.item

import de.raphaelgoetz.astralis.items.builder.SmartLoreBuilder
import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.schedule.doNow
import de.raphaelgoetz.astralis.schedule.doNowAsync
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.buildLite.cache.CacheReview
import de.raphaelgoetz.buildLite.dialog.review.showReviewDeletionDialog
import de.raphaelgoetz.buildLite.sql.RecordPlayerReview
import de.raphaelgoetz.buildLite.sql.submitReview
import de.raphaelgoetz.buildLite.sql.toSqlWorldOrNull
import de.raphaelgoetz.buildLite.world.WorldLoader
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta

fun Player.createReviewDisplayItem(record: RecordPlayerReview): SmartClick {
    val item = createSmartItem<ItemMeta>(
        name = record.title,
        material = Material.PAPER,
        interactionType = InteractionType.CLICK,
    ) {
        val description = getDescription()
        this.lore(description)
    }

    return SmartClick(item) { click ->
        click.isCancelled = true

        if (click.isShiftClick && click.isRightClick) {
            showReviewDeletionDialog(record)
            return@SmartClick
        }

        if (click.isLeftClick) {
            doNowAsync {
                //Only if the match was found. Then the world is probably not existing anymore
                val world = record.loadableLocation.worldUuid.toString().toSqlWorldOrNull() ?: return@doNowAsync

                doNow {
                    if (!isOnline) return@doNow
                    closeInventory()
                    WorldLoader.lazyTeleport(record.loadableLocation, world.generator, this)
                }
            }

            return@SmartClick
        }

        if (click.isRightClick) {
            submitReview(record)
            CacheReview.refresh(record)
        }

    }
}

private fun getDescription(): List<Component> {
    val lastSection = mutableListOf(
        "".gray(),
        "Left-Click > Teleport".gray(),
        "Right-Click > Resolve Review".gray(),
        "Shift + Right-Click > Delete Review".gray(),
    )
    return SmartLoreBuilder(lastSection).build()
}