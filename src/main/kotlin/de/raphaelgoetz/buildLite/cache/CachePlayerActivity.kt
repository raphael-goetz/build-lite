package de.raphaelgoetz.buildLite.cache

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Last-activity timestamps used to detect AFK players for build-time
 * tracking. Populated from movement/interaction listeners, read by the
 * periodic build-time sampler.
 */
object PlayerActivityCache {

    private val lastActivityMillis = ConcurrentHashMap<UUID, Long>()

    fun markActive(uuid: UUID) {
        lastActivityMillis[uuid] = System.currentTimeMillis()
    }

    fun isAfk(uuid: UUID, thresholdMillis: Long): Boolean {
        val lastActive = lastActivityMillis[uuid] ?: return true
        return System.currentTimeMillis() - lastActive >= thresholdMillis
    }

    fun flush(uuid: UUID) {
        lastActivityMillis.remove(uuid)
    }
}

fun Player.markActive() = PlayerActivityCache.markActive(uniqueId)

fun Player.isAfk(thresholdMillis: Long): Boolean = PlayerActivityCache.isAfk(uniqueId, thresholdMillis)
