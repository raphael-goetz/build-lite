package de.raphaelgoetz.buildLite.sanitiser

import de.raphaelgoetz.astralis.text.sendText
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.PREFIX
import io.papermc.paper.dialog.DialogResponseView
import org.bukkit.entity.Player

fun String.sanitiseNameInput(): String {
    val san = replace(Regex("\\W"), "")

    if (san.length > 254) {
        return san.take(254)
    }

    return san.lowercase()
}

fun String.sanitiseGroupInput(): String {
    val san = replace(Regex("\\W"), "")

    if (san.length > 254) {
        return san.take(254)
    }

    return san.lowercase()
}

fun DialogResponseView.getProtectedString(key: String, player: Player): String? {
    val value = getText(key)

    if (value == null) {
        player.sendText("$PREFIX An unexpected error occurred reading the key $key.") {
            color = Colorization.RED
        }
        return null
    }

    if (value.isEmpty()) {
        player.sendText("$PREFIX You can't leave the input: $key empty.") {
            color = Colorization.RED
        }
        return null
    }

    return value
}
