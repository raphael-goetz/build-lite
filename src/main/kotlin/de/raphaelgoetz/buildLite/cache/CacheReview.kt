package de.raphaelgoetz.buildLite.cache

import de.raphaelgoetz.buildLite.entity.Review
import de.raphaelgoetz.buildLite.sql.RecordPlayerReview
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player

object CacheReview {
    private var cache = mutableMapOf<World, MutableList<Review>>()

    fun getAll(): List<Review> {
        return cache.map { it.value }.flatten()
    }

    fun append(world: World, recordPlayerReview: RecordPlayerReview) {
        val review = Review(recordPlayerReview, world)
        review.spawn()

        for (player in PlayerCache.all()) {
            if (!player.recordPlayer.reviewMode) {
                val bukkitPlayer = Bukkit.getPlayer(player.playerUUID)
                bukkitPlayer?.let {
                    review.hideFor(it)
                }
            }
        }

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
            val r = Review(review, world)
            r.spawn()
            reviewEntities.add(r)

            for (player in PlayerCache.all()) {
                if (player.recordPlayer.reviewMode) continue
                val bukkitPlayer = Bukkit.getPlayer(player.playerUUID) ?: continue
                r.hideFor(bukkitPlayer)
            }
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
            entity.showFor(player)
        }
    }

    fun hideAll(player: Player) {
        for (entity in getAll()) {
            entity.hideFor(player)
        }
    }
}
