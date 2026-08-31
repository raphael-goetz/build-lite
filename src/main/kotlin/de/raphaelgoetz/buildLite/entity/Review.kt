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
        if (textDisplay?.isValid == true) return this

        val spawnLocation = location.clone().apply {
            y += 1
            yaw = 0f
            pitch = 0f
        }

        val entity = world.spawnEntity(spawnLocation, EntityType.TEXT_DISPLAY)
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

    /**
     * Review displays are derived from the database and deliberately not saved
     * into the world. Recreate the display if it was removed by a command or
     * another plugin while the world remained loaded.
     *
     * @return true when a replacement entity was spawned.
     */
    fun ensureSpawned(): Boolean {
        if (textDisplay?.isValid == true) return false
        textDisplay = null
        spawn()
        return textDisplay?.isValid == true
    }

    fun destroy() {
        textDisplay?.remove()
        textDisplay = null
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
