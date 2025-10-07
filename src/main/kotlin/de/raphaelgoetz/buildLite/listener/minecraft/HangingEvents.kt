package de.raphaelgoetz.buildLite.listener.minecraft

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.buildLite.listener.cancelWhenBuilder
import org.bukkit.entity.Player
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent

fun registerHangingEvents() {

    listen<HangingBreakByEntityEvent> { hangingBreakEvent ->
        val player = hangingBreakEvent.remover
        if (player is Player) {
            player.cancelWhenBuilder(hangingBreakEvent)
        }
    }

    listen<HangingPlaceEvent> { hangingPlaceEvent ->
        val player = hangingPlaceEvent.player ?: return@listen
        player.cancelWhenBuilder(hangingPlaceEvent)
    }

}