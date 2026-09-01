package de.raphaelgoetz.buildLite.sql

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/** Tracks R2 objects uploaded by the world-share command so an expired
 * presigned link's underlying object can be cleaned up afterwards. */
object SqlWorldUpload : Table("world_uploads") {
    val r2Key = varchar("r2_key", 255)
    val expiresAtEpochMs = long("expires_at_epoch_ms")

    override val primaryKey = PrimaryKey(r2Key)
}

data class RecordWorldUpload(
    val r2Key: String,
    val expiresAtEpochMs: Long,
)

fun createSqlWorldUpload(r2Key: String, expiresAtEpochMs: Long) = transaction {
    SqlWorldUpload.insert { record ->
        record[SqlWorldUpload.r2Key] = r2Key
        record[SqlWorldUpload.expiresAtEpochMs] = expiresAtEpochMs
    }
}

fun getExpiredSqlWorldUploads(nowEpochMs: Long): List<RecordWorldUpload> = transaction {
    SqlWorldUpload.selectAll().where { SqlWorldUpload.expiresAtEpochMs less nowEpochMs }.map { it.toRecordWorldUpload() }
}

fun RecordWorldUpload.deleteSqlWorldUpload() = transaction {
    SqlWorldUpload.deleteWhere { SqlWorldUpload.r2Key eq this@deleteSqlWorldUpload.r2Key }
}

private fun ResultRow.toRecordWorldUpload() = RecordWorldUpload(
    r2Key = this[SqlWorldUpload.r2Key],
    expiresAtEpochMs = this[SqlWorldUpload.expiresAtEpochMs],
)
