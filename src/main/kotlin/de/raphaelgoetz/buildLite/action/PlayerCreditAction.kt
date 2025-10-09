package de.raphaelgoetz.buildLite.action

import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.PREFIX
import de.raphaelgoetz.buildLite.player.checkPermission
import de.raphaelgoetz.buildLite.sql.createSqlPlayerCredit
import de.raphaelgoetz.buildLite.sql.deleteSqlPlayerCredit
import org.bukkit.entity.Player
import java.util.UUID

fun Player.actionAddCredit(playerUuid: UUID, worldUuid: UUID) {
    if (!checkPermission("build-lite.credit.add")) return
    createSqlPlayerCredit(playerUuid, worldUuid)
    sendMessage(adventureText("$PREFIX Credit added successfully.") {
        color = Colorization.LIME
    })
}

fun Player.actionRemoveCredit(playerUuid: UUID, worldUuid: UUID) {
    if (!checkPermission("build-lite.credit.remove")) return
    deleteSqlPlayerCredit(playerUuid, worldUuid)
    sendMessage(adventureText("$PREFIX Credit removed successfully.") {
        color = Colorization.LIME
    })
}
