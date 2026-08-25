package de.raphaelgoetz.buildLite

import de.raphaelgoetz.astralis.Astralis
import de.raphaelgoetz.buildLite.buildtime.BuildTimeTracker
import de.raphaelgoetz.buildLite.cache.PlayerProfileCache
import de.raphaelgoetz.buildLite.command.registerCommands
import de.raphaelgoetz.buildLite.config.PluginConfig
import de.raphaelgoetz.buildLite.listener.registerListener
import de.raphaelgoetz.buildLite.server.FileServer
import de.raphaelgoetz.buildLite.sql.SqlPlayer
import de.raphaelgoetz.buildLite.sql.SqlPlayerBuildTime
import de.raphaelgoetz.buildLite.sql.SqlPlayerCredit
import de.raphaelgoetz.buildLite.sql.SqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.SqlPlayerReview
import de.raphaelgoetz.buildLite.sql.SqlPlayerWarp
import de.raphaelgoetz.buildLite.sql.SqlWorld
import de.raphaelgoetz.buildLite.world.WorldLoader

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.net.SocketTimeoutException
import java.util.logging.Level

const val PREFIX = "[build-lite] >"

/**
 * Bukkit already keeps a single canonical instance per plugin class, retrievable
 * via JavaPlugin.getPlugin(). No need for a hand-rolled mutable global on top of it.
 */
fun buildLiteInstance(): BuildLite = JavaPlugin.getPlugin(BuildLite::class.java)

class BuildLite : Astralis() {
    var server: FileServer? = null
    private var dataSource: HikariDataSource? = null

    lateinit var spawnLocation: Location
        private set

    override fun enable() {
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
            config.getLong("build-time.afkThresholdSeconds", 180),
            config.getLong("build-time.tickIntervalSeconds", 20),
        )

        // Database.connect(url, driver) opens a brand new JDBC connection for every
        // transaction{} block and closes it afterward. A pooled/persistent connection
        // avoids that per-query connect/disconnect cost. SQLite only supports a single
        // writer, so the pool is intentionally sized to 1 rather than left at Hikari's
        // default of 10 (which would just serialize on SQLite's file lock anyway).
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = pluginConfig.dbUrl
            driverClassName = pluginConfig.dbDriver
            maximumPoolSize = 1
            connectionInitSql = "PRAGMA journal_mode=WAL;"

            // Most callers run on the main server thread. Hikari's default 30s
            // connectionTimeout would block it that long if the DB is unreachable,
            // risking the Paper watchdog killing the server. Fail fast instead.
            connectionTimeout = 3_000
        }
        dataSource = HikariDataSource(hikariConfig)

        Database.connect(dataSource!!)

        transaction {
            SchemaUtils.create(
                SqlPlayer, SqlPlayerBuildTime, SqlPlayerCredit, SqlPlayerFavorite, SqlPlayerReview, SqlPlayerWarp, SqlWorld
            )
        }

        this.spawnLocation = Location(
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

        PlayerProfileCache.init()
        BuildTimeTracker.start(pluginConfig.buildTimeAfkThresholdSeconds, pluginConfig.buildTimeTickIntervalSeconds)

        registerListener()
        registerCommands()
    }

    override fun disable() {
        BuildTimeTracker.stop()
        server?.stop()

        //For graceful shutdown!!!
        for (world in Bukkit.getWorlds()) {
            WorldLoader.lazyUnload(world)
        }

        dataSource?.close()
    }
}
