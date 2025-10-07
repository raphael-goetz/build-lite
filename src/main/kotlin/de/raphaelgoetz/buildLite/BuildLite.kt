package de.raphaelgoetz.buildLite

import de.raphaelgoetz.astralis.Astralis
import de.raphaelgoetz.buildLite.cache.PlayerProfileCache
import de.raphaelgoetz.buildLite.listener.registerListener
import de.raphaelgoetz.buildLite.sql.SqlPlayer
import de.raphaelgoetz.buildLite.sql.SqlPlayerCredit
import de.raphaelgoetz.buildLite.sql.SqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.SqlPlayerWarp
import de.raphaelgoetz.buildLite.sql.SqlWorld
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

val spawnLocation = Location(Bukkit.getWorld("world"), 0.0, 100.0, 0.0)

class BuildLite : Astralis() {
    override fun enable() {

        Database.connect(
            url = "jdbc:sqlite:worlds1.db", // path to your SQLite file
            driver = "org.sqlite.JDBC"
        )
        transaction {
            SchemaUtils.create(
                SqlPlayer, SqlPlayerCredit, SqlPlayerFavorite, SqlPlayerWarp, SqlWorld
            )
        }

        PlayerProfileCache.init()
        registerListener()
    }
}
