package de.raphaelgoetz.buildLite.cache

import com.destroystokyo.paper.profile.PlayerProfile
import com.google.gson.JsonParser
import de.raphaelgoetz.buildLite.sql.selectUniquePlayerUuids
import org.bukkit.Bukkit
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class CachePlayerProfile(
    val playerUUID: UUID, val playerName: String, val playerProfile: PlayerProfile
)

object PlayerProfileCache {

    var authAvailable = true
    private var cache = mutableMapOf<UUID, CachePlayerProfile>()



    fun init() {
        val uuids = selectUniquePlayerUuids()
        cache = mutableMapOf()

        for (uuid in uuids) {
            val name = getUsernameFromUUID(uuid)
            name?.let { result ->
                val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
                val profile = offlinePlayer.playerProfile.update().join()
                println("[build-lite] Cached the player's profile of: $result")
                cache[uuid] = CachePlayerProfile(uuid, result, profile)
            }
        }
    }

    fun getOrFetch(uuid: UUID): CachePlayerProfile {
        val optionalProfile = cache[uuid]

        if (optionalProfile != null) {
            return optionalProfile
        }

        val name = getUsernameFromUUID(uuid)
        name?.let { result ->
            val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
            val profile = offlinePlayer.playerProfile.update().join()
            println("[build-lite] Cached the player's profile of: $result")
            val cached = CachePlayerProfile(uuid, result, profile)
            cache[uuid] = cached
            return cached
        }

        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
        val profile = offlinePlayer.playerProfile.update().join()
        val cached = CachePlayerProfile(uuid, "Unknown", profile)
        cache[uuid] = cached
        return cached
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