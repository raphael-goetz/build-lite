package de.raphaelgoetz.buildLite.sql

import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

fun selectUniquePlayerUuids(): List<UUID> = transaction {
    val fromPlayers = SqlPlayer
        .select(SqlPlayer.uuid)
        .map { it[SqlPlayer.uuid] }

    val fromCredits = SqlPlayerCredit
        .select(SqlPlayerCredit.playerUUID)
        .map { it[SqlPlayerCredit.playerUUID] }

    (fromPlayers + fromCredits).distinct()
}