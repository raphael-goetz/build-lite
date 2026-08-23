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

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

const val PREFIX = "[build-lite] >"

lateinit var spawnLocation: Location
    private set

lateinit var BuildLiteInstance: BuildLite
    private set

class BuildLite : Astralis() {
    var server: FileServer? = null
    private var dataSource: HikariDataSource? = null

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

        PlayerProfileCache.init()

        registerListener()
        registerCommands()
    }

    override fun disable() {
        server?.stop()

        //For graceful shutdown!!!
        for (world in Bukkit.getWorlds()) {
            WorldLoader.lazyUnload(world)
        }

        dataSource?.close()
    }
}
