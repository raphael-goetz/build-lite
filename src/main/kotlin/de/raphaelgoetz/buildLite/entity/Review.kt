package de.raphaelgoetz.buildLite.entity

import de.raphaelgoetz.buildLite.buildLiteInstance
import de.raphaelgoetz.buildLite.cache.PlayerProfileCache
import de.raphaelgoetz.buildLite.player.createPlayerComponent
import de.raphaelgoetz.buildLite.player.createPlayerHead
import de.raphaelgoetz.buildLite.sql.RecordPlayerReview
import de.raphaelgoetz.buildLite.sql.getSqlPlayerReview
import de.raphaelgoetz.buildLite.world.toLocation
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.persistence.PersistentDataType

/** Tags every review entity so stale, already-persisted duplicates from before
 * entities were made non-persistent can be identified and purged on world load. */
val REVIEW_ENTITY_KEY: NamespacedKey = NamespacedKey(buildLiteInstance(), "review-entity")

data class Review(
    val recordPlayerReview: RecordPlayerReview, val world: World
) {

    val location: Location = recordPlayerReview.loadableLocation.toLocation(world)
    var textDisplay: TextDisplay? = null

    fun spawn(): Review {

        location.y += 1
        location.yaw = 0f
        location.pitch = 0f

        val entity = world.spawnEntity(location, EntityType.TEXT_DISPLAY)
        if (entity is TextDisplay) {
            // Without this, Minecraft's own periodic autosave can write this
            // entity to the world's region files independently of the plugin's
            // lifecycle. On an unclean shutdown (crash, kill -9, OOM) that copy
            // survives on disk and gets resurrected on the next world load --
            // on top of the fresh one CacheReview.loadWorld spawns -- duplicating
            // forever across every unclean restart. Marking it non-persistent
            // means it is never eligible for that disk write in the first place.
            entity.isPersistent = false
            entity.persistentDataContainer.set(REVIEW_ENTITY_KEY, PersistentDataType.INTEGER, recordPlayerReview.id)
            entity.renderText(recordPlayerReview)
            textDisplay = entity
        }

        return this
    }

    fun destroy() {
        textDisplay?.remove()
    }

    fun showFor(player: Player) {
        textDisplay?.let { textDisplay ->
            player.showEntity(buildLiteInstance(), textDisplay)
        }
    }

    fun hideFor(player: Player) {
        textDisplay?.let { textDisplay ->
            player.hideEntity(buildLiteInstance(), textDisplay)
        }
    }

    fun refresh() {
        textDisplay?.let { entity ->
            val review = getSqlPlayerReview(recordPlayerReview.id)
            entity.renderText(review)
        }
    }

    private fun TextDisplay.renderText(recordPlayerReview: RecordPlayerReview) {
        val creator = PlayerProfileCache.getOrFetch(recordPlayerReview.creatorUuid)
        var text = Component.text(recordPlayerReview.title).append(Component.newline())
        text = text.append(
            Component.text(recordPlayerReview.description).append(
                Component.newline().append(
                    Component.newline().append(
                        Component.text("Created by: ").append(creator.createPlayerComponent())
                    )
                )
            )
        )

        recordPlayerReview.reviewerUuid?.let { uuid ->
            val reviewer = PlayerProfileCache.getOrFetch(uuid)
            text = text.append(
                Component.newline().append(
                    Component.text("Reviewed by: ").append(
                        reviewer.createPlayerComponent()
                    )
                )
            )
        }

        text(text)
        isSeeThrough = true
        alignment = TextDisplay.TextAlignment.CENTER
        billboard = Display.Billboard.CENTER
    }

}