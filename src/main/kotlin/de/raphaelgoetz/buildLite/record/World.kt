package de.raphaelgoetz.buildLite.record

import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object Worlds : Table("worlds") {
    val id = uuid("id").autoGenerate()

    // Metadata
    val name = varchar("name", 255)
    val group = varchar("group", 255)
    val state = enumerationByName("state", WORLD_STATE_NAME_COLUMN_LENGTH, WorldState::class)

    // Spawn Data
    val spawnX = float("spawnX")
    val spawnY = float("spawnY")
    val spawnZ = float("spawnZ")
    val spawnPitch = float("spawnPitch")
    val spawnYaw = float("spawnYaw")
}

data class WorldRecord(
    val id: UUID,
    val name: String,
    val group: String,
    val state: WorldState,
    val spawnX: Float,
    val spawnY: Float,
    val spawnZ: Float,
    val spawnPitch: Float,
    val spawnYaw: Float
)


fun createWorldRecord(name: String, group: String = "unknown", state: WorldState = WorldState.NOT_STARTED): WorldRecord {
    return transaction {
        SchemaUtils.create(Worlds)

        val insertResult = Worlds.insert {
            it[Worlds.name] = name
            it[Worlds.group] = group
            it[Worlds.state] = state

            it[spawnX] = 0.0f
            it[spawnY] = 0.0f
            it[spawnZ] = 0.0f
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
