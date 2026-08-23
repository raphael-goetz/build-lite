package de.raphaelgoetz.buildLite.cache

import com.destroystokyo.paper.profile.PlayerProfile
import com.google.gson.JsonParser
import de.raphaelgoetz.buildLite.sql.selectUniquePlayerUuids
import org.bukkit.Bukkit
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

data class CachePlayerProfile(
    val playerUUID: UUID, val playerName: String, val playerProfile: PlayerProfile
)

object PlayerProfileCache {

    var authAvailable = true
    private val cache = ConcurrentHashMap<UUID, CachePlayerProfile>()
    private val pendingFetches = ConcurrentHashMap.newKeySet<UUID>()
    private val fetchExecutor = Executors.newFixedThreadPool(2) { runnable ->
        Thread(runnable, "build-lite-profile-cache-fetch").apply { isDaemon = true }
    }

    /**
     * Populates the cache in the background so plugin enable() isn't blocked by
     * one Mojang session-server round trip per known player.
     */
    fun init() {
        val uuids = selectUniquePlayerUuids()

        fetchExecutor.submit {
            for (uuid in uuids) {
                fetchAndCache(uuid)
            }
        }
    }

    /**
     * Returns the cached profile if present. On a cache miss, this returns an
     * immediate local-only placeholder (no network I/O) and kicks off a
     * background fetch so subsequent calls return the real profile — a caller
     * on the render/main thread is never blocked on a Mojang API round trip.
     */
    fun getOrFetch(uuid: UUID): CachePlayerProfile {
        val cached = cache[uuid]
        if (cached != null) {
            return cached
        }

        if (pendingFetches.add(uuid)) {
            fetchExecutor.submit {
                try {
                    fetchAndCache(uuid)
                } finally {
                    pendingFetches.remove(uuid)
                }
            }
        }

        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
        return CachePlayerProfile(uuid, offlinePlayer.name ?: "Unknown", offlinePlayer.playerProfile)
    }

    private fun fetchAndCache(uuid: UUID): CachePlayerProfile? {
        try {
            val name = getUsernameFromUUID(uuid) ?: Bukkit.getOfflinePlayer(uuid).name ?: "Unknown"
            val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
            val profile = offlinePlayer.playerProfile.update().join()
            val cached = CachePlayerProfile(uuid, name, profile)
            cache[uuid] = cached
            Bukkit.getLogger().info("[build-lite] Cached the player's profile of: $name")
            return cached
        } catch (e: SocketTimeoutException) {
            authAvailable = false
            Bukkit.getLogger().warning("[build-lite] Could not fetch profile for $uuid: Minecraft auth server is probably offline (${e.message})")
        } catch (e: Exception) {
            Bukkit.getLogger().warning("[build-lite] Could not fetch profile for $uuid: ${e.message}")
        }
        return null
    }

    private fun getUsernameFromUUID(uuid: UUID): String? {
        val url = URL("https://sessionserver.mojang.com/session/minecraft/profile/${uuid.toString().replace("-", "")}")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        if (connection.responseCode != 200) return null

        val json = connection.inputStream.bufferedReader().use { it.readText() }
        val element = JsonParser.parseString(json).asJsonObject
        return element["name"].asString
    }
}