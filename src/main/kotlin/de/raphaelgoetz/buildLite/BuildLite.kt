package de.raphaelgoetz.buildLite

import de.raphaelgoetz.astralis.Astralis
import de.raphaelgoetz.buildLite.cache.PlayerProfileCache
import de.raphaelgoetz.buildLite.command.registerCommands
import de.raphaelgoetz.buildLite.config.PluginConfig
import de.raphaelgoetz.buildLite.listener.registerListener
import de.raphaelgoetz.buildLite.server.FileServer
import de.raphaelgoetz.buildLite.sql.SqlPlayer
import de.raphaelgoetz.buildLite.sql.SqlPlayerCredit
import de.raphaelgoetz.buildLite.sql.SqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.SqlPlayerReview
import de.raphaelgoetz.buildLite.sql.SqlPlayerWarp
import de.raphaelgoetz.buildLite.sql.SqlWorld
import de.raphaelgoetz.buildLite.world.WorldLoader

import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.SocketTimeoutException
import java.util.logging.Level

const val PREFIX = "[build-lite] >"

lateinit var spawnLocation: Location
    private set

lateinit var BuildLiteInstance: BuildLite
    private set

class BuildLite : Astralis() {
    var server: FileServer? = null

    override fun enable() {
        BuildLiteInstance = this

        saveDefaultConfig()

        val pluginConfig = PluginConfig(
            config.getString("sql.driver", "org.sqlite.JDBC")!!,
            config.getString("sql.url", "jdbc:sqlite:worlds.db")!!,
            config.getBoolean("http.hasServer", false),
            config.getString("http.host", "localhost")!!,
            config.getInt("http.port", 8080),
            config.getDouble("location.x", 0.5),
            config.getDouble("location.y", 100.0),
            config.getDouble("location.z", 0.5),
            config.getDouble("location.yaw", 90.0).toFloat(),
            config.getDouble("location.pitch", 0.0).toFloat(),
        )

        Database.connect(
            url = pluginConfig.dbUrl,
            driver = pluginConfig.dbDriver
        )

        transaction {
            SchemaUtils.create(
                SqlPlayer, SqlPlayerCredit, SqlPlayerFavorite, SqlPlayerReview, SqlPlayerWarp, SqlWorld
            )
        }

        spawnLocation = Location(
            Bukkit.getWorld("world"),
            pluginConfig.spawnX,
            pluginConfig.spawnY,
            pluginConfig.spawnZ,
            pluginConfig.spawnPitch,
            pluginConfig.spawnYaw
        )

        if (pluginConfig.hasServer) {
            server = FileServer(pluginConfig)
            server?.start()
        }

        try {
            PlayerProfileCache.init()
        } catch (e: SocketTimeoutException) {
            PlayerProfileCache.authAvailable = false
            Bukkit.getLogger().log(
                Level.WARNING, "Could not initialize Player profile. Minecraft Auth Server are probably offline", e
            )
        }

        registerListener()
        registerCommands()
    }

    override fun disable() {
        server?.stop()

        //For graceful shutdown!!!
        for (world in Bukkit.getWorlds()) {
            WorldLoader.lazyUnload(world)
        }
    }
}
