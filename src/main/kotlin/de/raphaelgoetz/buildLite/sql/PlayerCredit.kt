package de.raphaelgoetz.buildLite.sql

import de.raphaelgoetz.buildLite.world.LoadableWorld
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object SqlPlayerCredit : Table("player_credits") {
    val id = integer("id").autoIncrement()

    val worldUUID = uuid("world_uuid")
    val playerUUID = uuid("player_uuid")

    override val primaryKey = PrimaryKey(id)
}

data class RecordPlayerCredit(
    val playerUuid: UUID,
    val worldUuid: UUID,
)

fun createSqlPlayerCredit(playerUUID: UUID, worldUuid: UUID) = transaction {
    val existing = SqlPlayerCredit.selectAll().where {
        (SqlPlayerCredit.playerUUID eq playerUUID) and (SqlPlayerCredit.worldUUID eq worldUuid)
    }.singleOrNull()

    if (existing == null) {
        SqlPlayerCredit.insert {
            it[SqlPlayerCredit.playerUUID] = playerUUID
            it[SqlPlayerCredit.worldUUID] = worldUuid
        }
    }

    getSqlPlayerCredit(playerUUID, worldUuid)
}

fun LoadableWorld.getSqlPlayerCredits(): List<RecordPlayerCredit> = transaction {
    SqlPlayerCredit
        .selectAll()
        .where { SqlPlayerCredit.worldUUID eq uniqueId }
        .map { record ->
            RecordPlayerCredit(
                playerUuid = record[SqlPlayerCredit.playerUUID],
                worldUuid = record[SqlPlayerCredit.worldUUID],
            )
        }
}

fun RecordWorld.deleteSqlPlayerCredits() = transaction {
    SqlPlayerCredit.deleteWhere { SqlPlayerCredit.worldUUID eq uniqueId }
}

fun deleteSqlPlayerCredit(playerUuid: UUID, worldUuid: UUID) = transaction {
    SqlPlayerCredit.deleteWhere { (SqlPlayerCredit.worldUUID eq worldUuid) and (SqlPlayerCredit.playerUUID eq playerUuid) }
}

private fun getSqlPlayerCredit(
    playerUuid: UUID, worldUuid: UUID
): RecordPlayerCredit = transaction {
    val record = SqlPlayerCredit.selectAll().where {
        (SqlPlayerCredit.playerUUID eq playerUuid) and (SqlPlayerCredit.worldUUID eq worldUuid)
    }.single()

    RecordPlayerCredit(
        playerUuid = record[SqlPlayerCredit.playerUUID],
        worldUuid = record[SqlPlayerCredit.worldUUID],
    )
}
