package de.raphaelgoetz.buildLite.sql

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDate
import java.util.UUID

object SqlPlayerBuildTime : Table("player_build_time") {
    val id = integer("id").autoIncrement()

    val playerUUID = javaUUID("player_uuid")
    val worldUUID = javaUUID("world_uuid")
    val day = varchar("day", 10)
    val seconds = long("seconds")

    override val primaryKey = PrimaryKey(id)
}

data class RecordPlayerBuildTime(
    val playerUuid: UUID,
    val worldUuid: UUID,
    val day: LocalDate,
    val seconds: Long,
)

data class BuildTimeCredit(
    val playerUuid: UUID,
    val worldUuid: UUID,
    val seconds: Long,
)

/**
 * Upserts the daily bucket for (player, world, day), adding [delta] seconds
 * to whatever is already recorded. Called once per sampler tick, so a bucket
 * usually already exists after the first hit of the day.
 */
fun addSqlBuildTimeSeconds(playerUuid: UUID, worldUuid: UUID, day: LocalDate, delta: Long) = transaction {
    addBuildTimeSeconds(playerUuid, worldUuid, day, delta)
}

/**
 * Writes one sampler pass in a single transaction. With SQLite's single
 * writer this avoids making UI reads wait behind one transaction per player.
 */
fun addSqlBuildTimeSeconds(credits: List<BuildTimeCredit>, day: LocalDate = LocalDate.now()) = transaction {
    credits.forEach { credit ->
        addBuildTimeSeconds(credit.playerUuid, credit.worldUuid, day, credit.seconds)
    }
}

private fun addBuildTimeSeconds(playerUuid: UUID, worldUuid: UUID, day: LocalDate, delta: Long) {
    val dayText = day.toString()
    val existing = SqlPlayerBuildTime.selectAll().where {
        (SqlPlayerBuildTime.playerUUID eq playerUuid) and
            (SqlPlayerBuildTime.worldUUID eq worldUuid) and
            (SqlPlayerBuildTime.day eq dayText)
    }.singleOrNull()

    if (existing == null) {
        SqlPlayerBuildTime.insert {
            it[SqlPlayerBuildTime.playerUUID] = playerUuid
            it[SqlPlayerBuildTime.worldUUID] = worldUuid
            it[SqlPlayerBuildTime.day] = dayText
            it[seconds] = delta
        }
    } else {
        SqlPlayerBuildTime.update({
            (SqlPlayerBuildTime.playerUUID eq playerUuid) and
                (SqlPlayerBuildTime.worldUUID eq worldUuid) and
                (SqlPlayerBuildTime.day eq dayText)
        }) {
            it[seconds] = existing[SqlPlayerBuildTime.seconds] + delta
        }
    }
}

/**
 * Seconds accrued per world for [playerUuid]. [since] filters to buckets on
 * or after that date; pass null for an all-time total.
 */
fun getSqlBuildTimeByWorld(playerUuid: UUID, since: LocalDate? = null): Map<UUID, Long> = transaction {
    val rows = if (since == null) {
        SqlPlayerBuildTime.selectAll().where { SqlPlayerBuildTime.playerUUID eq playerUuid }
    } else {
        SqlPlayerBuildTime.selectAll().where {
            (SqlPlayerBuildTime.playerUUID eq playerUuid) and (SqlPlayerBuildTime.day greaterEq since.toString())
        }
    }

    rows
        .groupBy { it[SqlPlayerBuildTime.worldUUID] }
        .mapValues { (_, group) -> group.sumOf { it[SqlPlayerBuildTime.seconds] } }
}
