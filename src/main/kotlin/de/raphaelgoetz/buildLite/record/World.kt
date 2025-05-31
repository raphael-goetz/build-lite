package de.raphaelgoetz.buildLite.record

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.update

object Worlds : Table("worlds") {
    val id = uuid("id").autoGenerate()

    // Metadata
    val name = varchar("name", 255)
    val group = varchar("group", 255)
    val state = enumerationByName("state", WORLD_STATE_NAME_COLUMN_LENGTH, WorldState::class)

    // Spawn Data
    val spawnX = double("spawnX")
    val spawnY = double("spawnY")
    val spawnZ = double("spawnZ")
    val spawnPitch = float("spawnPitch")
    val spawnYaw = float("spawnYaw")
}

data class WorldRecord(
    val id: UUID,
    var name: String,
    var group: String,
    var state: WorldState,
    var spawnX: Double,
    var spawnY: Double,
    var spawnZ: Double,
    var spawnPitch: Float,
    var spawnYaw: Float
)


fun createWorldRecord(name: String, group: String = "unknown", state: WorldState = WorldState.NOT_STARTED): WorldRecord {
    return transaction {
        SchemaUtils.create(Worlds)

        val insertResult = Worlds.insert {
            it[Worlds.name] = name
            it[Worlds.group] = group
            it[Worlds.state] = state

            it[spawnX] = 0.0
            it[spawnY] = 0.0
            it[spawnZ] = 0.0
            it[spawnPitch] = 0.0f
            it[spawnYaw] = 0.0f
        }

        WorldRecord(
            id = insertResult[Worlds.id],
            name = insertResult[Worlds.name],
            group = insertResult[Worlds.group],
            state = insertResult[Worlds.state],
            spawnX = insertResult[Worlds.spawnX],
            spawnY = insertResult[Worlds.spawnY],
            spawnZ = insertResult[Worlds.spawnZ],
            spawnPitch = insertResult[Worlds.spawnPitch],
            spawnYaw = insertResult[Worlds.spawnYaw]
        )
    }
}

fun getWorldRecords(): List<WorldRecord> {
    return transaction {
        Worlds.selectAll().map {
            WorldRecord(
                id = it[Worlds.id],
                name = it[Worlds.name],
                group = it[Worlds.group],
                state = it[Worlds.state],
                spawnX = it[Worlds.spawnX],
                spawnY = it[Worlds.spawnY],
                spawnZ = it[Worlds.spawnZ],
                spawnPitch = it[Worlds.spawnPitch],
                spawnYaw = it[Worlds.spawnYaw]
            )
        }
    }
}

fun WorldRecord.updateWorldRecord() {
    transaction {
        Worlds.update({ Worlds.id eq this@updateWorldRecord.id }) {
            it[Worlds.name] = this@updateWorldRecord.name
            it[Worlds.group] = this@updateWorldRecord.group
            it[Worlds.state] = this@updateWorldRecord.state
            it[Worlds.spawnX] = this@updateWorldRecord.spawnX
            it[Worlds.spawnY] = this@updateWorldRecord.spawnY
            it[Worlds.spawnZ] = this@updateWorldRecord.spawnZ
            it[Worlds.spawnPitch] = this@updateWorldRecord.spawnPitch
            it[Worlds.spawnYaw] = this@updateWorldRecord.spawnYaw
        }
    }

    getWorldRecords().forEach {
        println(it.toString())
    }
}

fun WorldRecord.updateStatus(status: WorldState) {
    state = status
    updateWorldRecord()
}

fun WorldRecord.updateGroup(group: String) {
    this.group = group
    updateWorldRecord()
}

fun WorldRecord.updateName(name: String) {
    this.name = name
    updateWorldRecord()
}

fun WorldRecord.updateSpawnLocation(location: org.bukkit.Location) {
    this.spawnX = location.x
    this.spawnY = location.y
    this.spawnZ = location.z
    this.spawnPitch = location.pitch
    this.spawnYaw = location.yaw
    updateWorldRecord()
}

fun String.isWorldRecord(): Boolean {
    try {
        val uuid = UUID.fromString(this)
        return transaction {
            return@transaction !(Worlds.select(Worlds.id eq uuid).limit(1).empty())
        }
    } catch (_: IllegalArgumentException) {
        return false
    }
}
