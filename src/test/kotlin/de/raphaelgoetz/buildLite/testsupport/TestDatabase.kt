package de.raphaelgoetz.buildLite.testsupport

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.raphaelgoetz.buildLite.sql.SqlPlayerBuildTime
import de.raphaelgoetz.buildLite.sql.SqlPlayerCredit
import de.raphaelgoetz.buildLite.sql.SqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.SqlWorld
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/**
 * A single physical in-memory SQLite connection, kept alive for the whole test
 * run via a pool size of 1 (mirrors production HikariConfig in BuildLite.kt) --
 * SQLite's :memory: database only exists for the lifetime of its one connection,
 * so a normal pool (or Database.connect(url, driver), which opens/closes a new
 * connection per transaction) would wipe the schema between every transaction.
 */
object TestDatabase {
    private var dataSource: HikariDataSource? = null

    fun connect() {
        if (dataSource != null) return

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite::memory:"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 1
        }
        dataSource = HikariDataSource(hikariConfig)

        Database.connect(dataSource!!)

        transaction {
            SchemaUtils.create(SqlWorld, SqlPlayerFavorite, SqlPlayerCredit, SqlPlayerBuildTime)
        }
    }

    fun clear() {
        transaction {
            SqlPlayerBuildTime.deleteAll()
            SqlPlayerCredit.deleteAll()
            SqlPlayerFavorite.deleteAll()
            SqlWorld.deleteAll()
        }
    }
}
