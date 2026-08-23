package de.raphaelgoetz.buildLite.sql

import de.raphaelgoetz.buildLite.world.LoadableLocation
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

object SqlPlayerWarp : Table("player_warps") {
    val id = integer("id").autoIncrement()

    val worldUUID = javaUUID("world_uuid")
    val playerUUID = javaUUID("player_uuid")
    val isPrivate = bool("is_private")

    val name = varchar("name", 255)

    val x = double("x")
    val y = double("y")
    val z = double("z")
    val pitch = float("pitch")
    val yaw = float("yaw")

    override val primaryKey = PrimaryKey(id)
}

data class RecordPlayerWarp(
    val id: Int,
    val playerUuid: UUID,
    val worldUuid: UUID,
    val name: String,
    val location: LoadableLocation,
    val isPrivate: Boolean,
)

fun Player.getSqlPlayerWarps(isPrivate: Boolean = false): List<RecordPlayerWarp> = transaction {
    val condition = if (isPrivate) (SqlPlayerWarp.playerUUID eq uniqueId) and (SqlPlayerWarp.isPrivate eq true)
    else SqlPlayerWarp.isPrivate eq false

    SqlPlayerWarp.selectAll().where { condition }.map { it.toRecordPlayerWarp() }
}

fun Player.createSqlPlayerWarp(
    loadableLocation: LoadableLocation, name: String, isPrivate: Boolean = false
) = transaction {
    SqlPlayerWarp.insert {
        it[SqlPlayerWarp.worldUUID] = loadableLocation.worldUuid
        it[playerUUID] = uniqueId
        it[SqlPlayerWarp.isPrivate] = isPrivate
        it[SqlPlayerWarp.name] = name
        it[x] = loadableLocation.x
        it[y] = loadableLocation.y
        it[z] = loadableLocation.z
        it[yaw] = loadableLocation.yaw
        it[pitch] = loadableLocation.pitch
    }
}

fun RecordPlayerWarp.deleteSqlPlayerWarp() = transaction {
    SqlPlayerWarp.deleteWhere { SqlPlayerWarp.id eq id }
}

fun RecordWorld.deleteSqlPlayerWarps() = transaction {
    SqlPlayerWarp.deleteWhere { SqlPlayerWarp.worldUUID eq uniqueId }
}

private fun ResultRow.toRecordPlayerWarp() = RecordPlayerWarp(
    id = this[SqlPlayerWarp.id],
    worldUuid = this[SqlPlayerWarp.worldUUID],
    playerUuid = this[SqlPlayerWarp.playerUUID],
    name = this[SqlPlayerWarp.name],
    isPrivate = this[SqlPlayerWarp.isPrivate],
    location = LoadableLocation(
        worldUuid = this[SqlPlayerWarp.worldUUID],
        x = this[SqlPlayerWarp.x],
        y = this[SqlPlayerWarp.y],
        z = this[SqlPlayerWarp.z],
        pitch = this[SqlPlayerWarp.pitch],
        yaw = this[SqlPlayerWarp.yaw],
    )
)