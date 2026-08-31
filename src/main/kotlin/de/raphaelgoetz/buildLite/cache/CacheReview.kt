package de.raphaelgoetz.buildLite.cache

import de.raphaelgoetz.buildLite.entity.Review
import de.raphaelgoetz.buildLite.sql.RecordPlayerReview
import de.raphaelgoetz.buildLite.world.toLocation
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay

object CacheReview {
    private var cache = mutableMapOf<World, MutableList<Review>>()

    fun getAll(): List<Review> {
        return cache.map { it.value }.flatten()
    }

    fun append(world: World, recordPlayerReview: RecordPlayerReview) {
        val review = Review(recordPlayerReview, world)
        review.spawn()
        applyVisibility(review)

        val list = cache[world]
        if (list != null) {
            list.add(review)
            return
        }

        cache[world] = mutableListOf(review)
    }

    fun refresh(recordPlayerReview: RecordPlayerReview) {
        cache.forEach { (_, reviews) ->
            reviews.forEach { review ->
                if (review.recordPlayerReview.id == recordPlayerReview.id) {
                    ensureSpawned(review)
                    review.refresh()
                    return@forEach
                }
            }
        }
    }

    fun remove(recordPlayerReview: RecordPlayerReview) {
        cache.forEach { (_, reviews) ->
            val iterator = reviews.iterator()
            while (iterator.hasNext()) {
                val review = iterator.next()
                if (review.recordPlayerReview.id == recordPlayerReview.id) {
                    review.destroy()
                    iterator.remove()
                }
            }
        }
    }

    fun loadWorld(world: World, reviews: List<RecordPlayerReview>) {
        val reviewEntities = mutableListOf<Review>()

        for (review in reviews) {
            // One-time cleanup: build-lite is the only thing that ever spawns a
            // TextDisplay at a review's exact recorded location, so anything
            // already sitting there -- tagged or not -- is a leftover duplicate.
            // This also catches entities spawned before entities were made
            // non-persistent (no tag existed yet), not just future regressions.
            purgeStaleReviewEntitiesAt(world, review)

            val r = Review(review, world)
            r.spawn()
            reviewEntities.add(r)
            applyVisibility(r)
        }

        cache[world] = reviewEntities
    }

    fun unloadWorld(world: World) {
        val reviewEntities = cache[world] ?: return

        for (review in reviewEntities) {
            review.destroy()
        }

        cache.remove(world)
    }

    fun showAll(player: Player) {
        for (entity in getAll()) {
            ensureSpawned(entity)
            entity.showFor(player)
        }
    }

    fun hideAll(player: Player) {
        for (entity in getAll()) {
            ensureSpawned(entity)
            entity.hideFor(player)
        }
    }

    private fun ensureSpawned(review: Review) {
        if (review.ensureSpawned()) applyVisibility(review)
    }

    private fun applyVisibility(review: Review) {
        for (player in PlayerCache.all()) {
            if (player.recordPlayer.reviewMode) continue
            val bukkitPlayer = Bukkit.getPlayer(player.playerUUID) ?: continue
            review.hideFor(bukkitPlayer)
        }
    }

    private const val STALE_ENTITY_SEARCH_RADIUS = 0.5

    private fun purgeStaleReviewEntitiesAt(world: World, review: RecordPlayerReview) {
        val spawnLocation = review.loadableLocation.toLocation(world).apply { y += 1 }

        world.getNearbyEntities(
            spawnLocation, STALE_ENTITY_SEARCH_RADIUS, STALE_ENTITY_SEARCH_RADIUS, STALE_ENTITY_SEARCH_RADIUS
        )
            .filterIsInstance<TextDisplay>()
            .filter { it.matchesReviewBody(review) }
            .forEach { it.remove() }
    }

    /**
     * Position alone isn't a strong enough signal to remove someone else's
     * entity -- also require the displayed text to actually be this review's
     * content (renderText always writes the title as the very first line).
     */
    private fun TextDisplay.matchesReviewBody(review: RecordPlayerReview): Boolean {
        val plainText = PlainTextComponentSerializer.plainText().serialize(text())
        return plainText.startsWith(review.title) && plainText.contains(review.description)
    }
}
