package de.raphaelgoetz.buildLite.sql

import de.raphaelgoetz.buildLite.sql.SqlPlayerFavorite.player_uuid
import de.raphaelgoetz.buildLite.sql.SqlPlayerFavorite.world_uuid
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object SqlPlayerFavorite : Table("player_favorites") {
    val id = integer("id").autoIncrement()

    val world_uuid = uuid("world_uuid")
    val player_uuid = uuid("player_uuid")

    override val primaryKey = PrimaryKey( id)
}

data class RecordPlayerFavorite(
    val playerUuid: UUID,
    val worldUuid: UUID,
)

fun Player.createSqlPlayerFavorite(worldUuid: UUID): RecordPlayerFavorite = transaction {
    val existing = SqlPlayerFavorite.selectAll().where {
        (player_uuid eq uniqueId) and (world_uuid eq worldUuid)
    }.singleOrNull()

    if (existing == null) {
        SqlPlayerFavorite.insert {
            it[player_uuid] = uniqueId
            it[world_uuid] = worldUuid
        }
    }

    getSqlPlayerFavorite(worldUuid)
}

fun Player.deleteSqlPlayerFavorite(worldUuid: UUID) = transaction {
    SqlPlayerFavorite.deleteWhere {
        (player_uuid eq uniqueId) and (world_uuid eq worldUuid)
    }
}

fun Player.hasSqlPlayerFavorite(worldUuid: UUID): Boolean = transaction {
    val exists = SqlPlayerFavorite.selectAll().where {
        (player_uuid eq uniqueId) and (world_uuid eq worldUuid)
    }.singleOrNull()

    return@transaction exists != null
}

fun Player.getSqlPlayerFavorite(worldUuid: UUID): RecordPlayerFavorite = transaction {
    val record = SqlPlayerFavorite.selectAll().where {
        (player_uuid eq uniqueId) and (world_uuid eq worldUuid)
    }.single()

    RecordPlayerFavorite(
        record[player_uuid],
        record[world_uuid]
    )
}
