package de.raphaelgoetz.buildLite.sql

import de.raphaelgoetz.buildLite.world.LoadableWorld

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

import java.util.UUID

object SqlPlayerCredit : Table("player_credits") {
    val id = integer("id").autoIncrement()

    val worldUUID = javaUUID("world_uuid")
    val playerUUID = javaUUID("player_uuid")

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

fun RecordWorld.getSqlPlayerCredits(): List<RecordPlayerCredit> = transaction {
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

fun getSqlPlayerCreditsFor(worldUuids: Collection<UUID>): Map<UUID, List<RecordPlayerCredit>> {
    if (worldUuids.isEmpty()) return emptyMap()

    return transaction {
        SqlPlayerCredit
            .selectAll()
            .where { SqlPlayerCredit.worldUUID inList worldUuids }
            .map { record ->
                RecordPlayerCredit(
                    playerUuid = record[SqlPlayerCredit.playerUUID],
                    worldUuid = record[SqlPlayerCredit.worldUUID],
                )
            }
            .groupBy { it.worldUuid }
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
