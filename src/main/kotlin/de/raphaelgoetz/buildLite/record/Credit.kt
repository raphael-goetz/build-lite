package de.raphaelgoetz.buildLite.record

import de.raphaelgoetz.buildLite.store.BuildWorld
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object Credits : Table("credits") {
    val id = integer("id").autoIncrement()

    val worldUUID = uuid("world_uuid")
    val playerUUID = uuid("player_uuid")
}

data class CreditRecord(val worldUUID: UUID, val playerUUID: UUID)

fun BuildWorld.getCredits(): List<CreditRecord> {
    return transaction {
       Credits
        .selectAll()
        .where { Credits.worldUUID eq this@getCredits.meta.id }
        .map {
            CreditRecord(
                worldUUID = it[Credits.worldUUID],
                playerUUID = it[Credits.playerUUID],
            )
        }
    }
}

fun BuildWorld.addCredit(playerUUID: UUID) {
    transaction {
        val isEmpty = Credits.selectAll().where {
            (Credits.worldUUID eq this@addCredit.meta.id) and (Credits.playerUUID eq playerUUID)
        }.limit(1).empty()

        if (isEmpty) {
            Credits.insert {
                it[Credits.worldUUID] = this@addCredit.meta.id
                it[Credits.playerUUID] = playerUUID
            }
        }
    }
}

fun BuildWorld.removeCredit(playerUUID: UUID) {
    transaction {
        Credits.deleteWhere {
            (Credits.worldUUID eq this@removeCredit.meta.id) and (Credits.playerUUID eq playerUUID)
        }
    }
}

fun BuildWorld.removeAllCredits() {
    transaction {
        Credits.deleteWhere {
            Credits.worldUUID eq this@removeAllCredits.meta.id
        }
    }
}