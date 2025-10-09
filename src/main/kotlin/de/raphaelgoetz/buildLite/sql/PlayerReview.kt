package de.raphaelgoetz.buildLite.sql

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

object SqlPlayerReview : Table("player_reviews") {
    val id = integer("id").autoIncrement()

    val creatorUuid = uuid("player_uuid")
    val reviewerUuid = uuid("reviewer_uuid").nullable()

    val title = varchar("title", 255)
    val description = varchar("description", 255)

    // Last Known Location
    val locationWorld = uuid("review_world")
    val locationX = double("review_x")
    val locationY = double("review_y")
    val locationZ = double("review_z")
    val locationPitch = float("review_pitch")
    val locationYaw = float("review_yaw")

    override val primaryKey = PrimaryKey(id)
}

data class RecordPlayerReview(
    val id: Int,
    val creatorUuid: UUID,
    val description: String,
    val title: String,
    val reviewerUuid: UUID?,
    val loadableLocation: LoadableLocation
)

fun Player.createSqlPlayerReview(title: String, description: String): RecordPlayerReview? = transaction {
    try {
        val worldUuid = UUID.fromString(location.world.name)
        val result = SqlPlayerReview.insert {
            it[SqlPlayerReview.creatorUuid] = uniqueId
            it[SqlPlayerReview.title] = title
            it[SqlPlayerReview.description] = description
            it[SqlPlayerReview.locationWorld] = worldUuid
            it[SqlPlayerReview.locationX] = location.x
            it[SqlPlayerReview.locationY] = location.y
            it[SqlPlayerReview.locationZ] = location.z
            it[SqlPlayerReview.locationPitch] = location.pitch
            it[SqlPlayerReview.locationYaw] = location.yaw
        }

        result.resultedValues?.firstOrNull()?.toRecordWorld()
    } catch (_: IllegalStateException) {
        null
    }
}

fun getSqlPlayerReview(id: Int): RecordPlayerReview = transaction {
    SqlPlayerReview.selectAll().where { SqlPlayerReview.id eq id }.first().toRecordWorld()
}

fun getSqlPlayerReviews(worldUuid: UUID): List<RecordPlayerReview> = transaction {
    SqlPlayerReview.selectAll().where { SqlPlayerReview.locationWorld eq worldUuid }.map { it.toRecordWorld() }
}

fun RecordPlayerReview.deleteSqlPlayerReview() = transaction {
    val uniqueID = this@deleteSqlPlayerReview.id
    SqlPlayerReview.deleteWhere { SqlPlayerReview.id eq uniqueID }
}

fun Player.submitReview(recordPlayerReview: RecordPlayerReview) = transaction {
    SqlPlayerReview.update({ SqlPlayerReview.id eq recordPlayerReview.id }) {
        it[reviewerUuid] = uniqueId
    }
}

private fun ResultRow.toRecordWorld() = RecordPlayerReview(
    reviewerUuid = this[SqlPlayerReview.reviewerUuid],
    creatorUuid = this[SqlPlayerReview.creatorUuid],
    description = this[SqlPlayerReview.description],
    loadableLocation = LoadableLocation(
        worldUuid = this[SqlPlayerReview.locationWorld],
        x = this[SqlPlayerReview.locationX],
        y = this[SqlPlayerReview.locationY],
        z = this[SqlPlayerReview.locationZ],
        pitch = this[SqlPlayerReview.locationPitch],
        yaw = this[SqlPlayerReview.locationYaw],
    ),
    id = this[SqlPlayerReview.id],
    title = this[SqlPlayerReview.title],
)
