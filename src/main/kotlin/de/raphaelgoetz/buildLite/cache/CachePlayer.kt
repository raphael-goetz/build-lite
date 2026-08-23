package de.raphaelgoetz.buildLite.cache

import de.raphaelgoetz.buildLite.sql.RecordPlayer
import de.raphaelgoetz.buildLite.sql.initSqlPlayer
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class CachePlayer(
    val playerUUID: UUID, val recordPlayer: RecordPlayer
)

object PlayerCache {

    // Accessed from both the main thread and async tasks now that callers fetch
    // player data off-thread before rendering, so this must be a thread-safe map.
    private val players = ConcurrentHashMap<UUID, CachePlayer>()

    fun all() = players.values.toList()

    fun getOrInit(player: Player): CachePlayer {
        val optional = players.get(player.uniqueId)

        if (optional != null) {
            return optional
        }

        val record = player.initSqlPlayer()
        val player = CachePlayer(player.uniqueId, record)
        players[player.playerUUID] = player
        return player
    }

    fun flush(player: Player) {
        players.remove(player.uniqueId)
    }

    fun refresh(player: Player) {
        players.remove(player.uniqueId)
        getOrInit(player)
    }
}