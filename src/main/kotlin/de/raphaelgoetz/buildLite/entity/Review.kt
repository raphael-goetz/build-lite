package de.raphaelgoetz.buildLite.entity

import de.raphaelgoetz.buildLite.BuildLiteInstance
import de.raphaelgoetz.buildLite.cache.PlayerProfileCache
import de.raphaelgoetz.buildLite.player.createPlayerHead
import de.raphaelgoetz.buildLite.sql.RecordPlayerReview
import de.raphaelgoetz.buildLite.sql.getSqlPlayerReview
import de.raphaelgoetz.buildLite.world.toLocation
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay

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
            entity.renderText(recordPlayerReview)
            textDisplay = entity
        }

        return this
    }

    fun destroy() {
        println("trying to delete the entity")
        textDisplay?.remove()
    }

    fun showFor(player: Player) {
        textDisplay?.let { textDisplay ->
            player.showEntity(BuildLiteInstance, textDisplay)
        }
    }

    fun hideFor(player: Player) {
        textDisplay?.let { textDisplay ->
            player.hideEntity(BuildLiteInstance, textDisplay)
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
                        Component.text("Created by: ").append(
                            createPlayerHead(creator).append {
                                Component.text(creator.playerName)
                            })
                    )
                )
            )
        )

        recordPlayerReview.reviewerUuid?.let { uuid ->
            val reviewer = PlayerProfileCache.getOrFetch(uuid)
            text = text.append(
                Component.newline().append(
                    Component.text("Reviewed by: ").append(
                        createPlayerHead(reviewer).append {
                            Component.text(reviewer.playerName)
                        })
                )
            )
        }

        text(text)
        isSeeThrough = true
        alignment = TextDisplay.TextAlignment.CENTER
        billboard = Display.Billboard.CENTER
    }

}