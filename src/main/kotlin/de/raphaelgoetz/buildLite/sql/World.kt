package de.raphaelgoetz.buildLite.sql

import de.raphaelgoetz.buildLite.hasWorldEnterPermission
import de.raphaelgoetz.buildLite.sql.types.WORLD_GENERATOR_NAME_COLUMN_LENGTH
import de.raphaelgoetz.buildLite.sql.types.WORLD_STATE_NAME_COLUMN_LENGTH
import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.WorldState
import de.raphaelgoetz.buildLite.world.LoadableLocation
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import kotlin.let

object SqlWorld : Table("worlds") {
    val uuid = uuid("uuid")

    // Metadata
    val name = varchar("name", 255)
    val group = varchar("group", 255)
    val state = enumerationByName("state", WORLD_STATE_NAME_COLUMN_LENGTH, WorldState::class)
    val generator = enumerationByName("generator", WORLD_GENERATOR_NAME_COLUMN_LENGTH, WorldGenerator::class)
    val creatorUuid = uuid("creator_uuid")
    val physicsEnabled = bool("physics_enabled")

    // Spawn Data
    val spawnX = double("spawn_x")
    val spawnY = double("spawn_y")
    val spawnZ = double("spawn_z")
    val spawnPitch = float("spawn_pitch")
    val spawnYaw = float("spawn_yaw")

    override val primaryKey = PrimaryKey(uuid)
}

data class RecordWorld(
    val name: String,
    val group: String,
    val state: WorldState,
    val creatorUuid: UUID,
    val uniqueId: UUID,
    val physicsEnabled: Boolean,
    val generator: WorldGenerator,
    val loadableSpawn: LoadableLocation
)

fun Player.createSqlWorld(
    name: String,
    group: String,
    generator: WorldGenerator,
    state: WorldState,
): RecordWorld = transaction {
    val uuid = UUID.randomUUID()

    SqlWorld.insert { record ->
        record[SqlWorld.uuid] = uuid
        record[SqlWorld.name] = name
        record[SqlWorld.group] = group
        record[SqlWorld.state] = state
        record[SqlWorld.generator] = generator
        record[SqlWorld.creatorUuid] = uniqueId
        record[SqlWorld.physicsEnabled] = false

        record[spawnX] = 0.0
        record[spawnY] = 64.0
        record[spawnZ] = 0.0
        record[spawnPitch] = 0f
        record[spawnYaw] = 0f
    }

    getSqlWorld(uuid)
}

fun RecordWorld.updateSqlWorld(
    name: String? = null,
    group: String? = null,
    spawn: LoadableLocation? = null,
    physicsEnabled: Boolean? = null,
    state: WorldState? = null,
    generator: WorldGenerator? = null,
): RecordWorld = transaction {
    SqlWorld.update({ SqlWorld.uuid eq uniqueId }) { record ->
        name?.let { value -> record[SqlWorld.name] = value }
        group?.let { value -> record[SqlWorld.group] = value }
        physicsEnabled?.let { value -> record[SqlWorld.physicsEnabled] = value }
        state?.let { value -> record[SqlWorld.state] = value }
        generator?.let { value -> record[SqlWorld.generator] = value }
        spawn?.let { value ->
            record[spawnX] = value.x
            record[spawnY] = value.y
            record[spawnZ] = value.z
            record[spawnPitch] = value.pitch
            record[spawnYaw] = value.yaw
        }
    }

    getSqlWorld(uniqueId)
}

fun RecordWorld.deleteSqlWorld() = transaction {
    SqlWorld.deleteWhere { SqlWorld.uuid eq uniqueId }
}

fun Player.getAccessibleSqlWorlds(): List<RecordWorld> = transaction {
    SqlWorld
        .selectAll()
        .map { it.toRecordWorld() }
        .filter { hasWorldEnterPermission(it.name, it.group) }
}

fun getAllSqlWorlds(): List<RecordWorld> = transaction {
    SqlWorld.selectAll().map { it.toRecordWorld() }
}

private fun ResultRow.toRecordWorld() = RecordWorld(
    uniqueId = this[SqlWorld.uuid],
    creatorUuid = this[SqlWorld.creatorUuid],
    name = this[SqlWorld.name],
    group = this[SqlWorld.group],
    loadableSpawn = LoadableLocation(
        worldUuid = this[SqlWorld.uuid],
        x = this[SqlWorld.spawnX],
        y = this[SqlWorld.spawnY],
        z = this[SqlWorld.spawnZ],
        pitch = this[SqlWorld.spawnPitch],
        yaw = this[SqlWorld.spawnYaw],
    ),
    generator = this[SqlWorld.generator],
    physicsEnabled = this[SqlWorld.physicsEnabled],
    state = this[SqlWorld.state],
)

private fun getSqlWorld(uuid: UUID): RecordWorld = transaction {
    return@transaction SqlWorld.selectAll().where { SqlWorld.uuid eq uuid }.single().toRecordWorld()
}