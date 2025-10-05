package de.raphaelgoetz.buildLite.action

import de.raphaelgoetz.buildLite.sql.createSqlPlayerCredit
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerCredit
import org.bukkit.entity.Player
import java.util.UUID

fun Player.actionAddCredit(playerUuid: UUID, worldUuid: UUID) {
    if (!hasPermission("build-lite.credit.add") || !hasPermission("build-lite.*")) {
        sendMessage("You do not have permission to create this world.")
        return
    }

    createSqlPlayerCredit(playerUuid, worldUuid)
}

fun Player.actionRemoveCredit(playerUuid: UUID, worldUuid: UUID) {
    if (!hasPermission("build-lite.credit.remove") || !hasPermission("build-lite.*")) {
        sendMessage("You do not have permission to create this world.")
        return
    }

    deleteSqlPlayerCredit(playerUuid, worldUuid)
}
