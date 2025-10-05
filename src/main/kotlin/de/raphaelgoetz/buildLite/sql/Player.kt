package de.raphaelgoetz.buildLite.sql

import de.raphaelgoetz.buildLite.sql.SqlPlayer.uuid
import de.raphaelgoetz.buildLite.world.LoadableLocation
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

object SqlPlayer : Table("players") {
    val uuid = uuid("id")

    // Modes
    val nightMode = bool("night_mode")
    val buildMode = bool("build_mode")

    // Last Known Location
    val locationWorld = uuid("spawn_world").nullable()
    val locationX = double("spawn_x").nullable()
    val locationY = double("spawn_y").nullable()
    val locationZ = double("spawn_z").nullable()
    val locationPitch = float("spawn_pitch").nullable()
    val locationYaw = float("spawn_yaw").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(uuid)
}

data class RecordPlayer(
    val uuid: UUID,
    val nightMode: Boolean,
    val buildMode: Boolean,
    val lastKnownLocation: LoadableLocation?,
)

fun Player.initSqlPlayer(): RecordPlayer = transaction {
    val existing = SqlPlayer.selectAll()
        .where { uuid eq uniqueId }
        .singleOrNull()

    if (existing == null) {
        SqlPlayer.insert {
            it[uuid] = uniqueId
            it[SqlPlayer.nightMode] = false
            it[SqlPlayer.buildMode] = false
        }
    }

    getSqlPlayer()
}

fun Player.updateSqlPlayer(
    buildMode: Boolean? = null,
    nightMode: Boolean? = null,
    location: LoadableLocation? = null
): RecordPlayer = transaction {
    SqlPlayer.update({ uuid eq uniqueId }) {
        buildMode?.let { mode -> it[SqlPlayer.buildMode] = mode }
        nightMode?.let { mode -> it[SqlPlayer.nightMode] = mode }
        location?.let { location ->
            it[SqlPlayer.locationWorld] = location.worldUuid
            it[SqlPlayer.locationX] = location.x
            it[SqlPlayer.locationY] = location.y
            it[SqlPlayer.locationZ] = location.z
            it[SqlPlayer.locationPitch] = location.pitch
            it[SqlPlayer.locationYaw] = location.yaw
        }
    }
    getSqlPlayer()
}

fun Player.getSqlPlayer(): RecordPlayer = transaction {
    val record = SqlPlayer.selectAll().where { uuid eq uniqueId }.single()

    RecordPlayer(
        uuid = record[uuid],
        nightMode = record[SqlPlayer.nightMode],
        buildMode = record[SqlPlayer.buildMode],
        lastKnownLocation = run {
            val worldUuid = record[SqlPlayer.locationWorld]
            val x = record[SqlPlayer.locationX]
            val y = record[SqlPlayer.locationY]
            val z = record[SqlPlayer.locationZ]
            val pitch = record[SqlPlayer.locationPitch]
            val yaw = record[SqlPlayer.locationYaw]

            if (worldUuid == null || x == null || y == null || z == null || pitch == null || yaw == null) {
                null
            } else {
                LoadableLocation(
                    worldUuid = worldUuid,
                    x = x,
                    y = y,
                    z = z,
                    pitch = pitch,
                    yaw = yaw
                )
            }
        }
    )

}