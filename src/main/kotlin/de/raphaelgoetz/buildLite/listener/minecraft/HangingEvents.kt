package de.raphaelgoetz.buildLite.listener.minecraft

import de.raphaelgoetz.buildLite.store.BuildServer

import de.raphaelgoetz.astralis.event.listen
import org.bukkit.entity.Player
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent

fun registerHangingEvents(server: BuildServer) {

    listen<HangingBreakByEntityEvent> { hangingBreakEvent ->

        val player = hangingBreakEvent.entity
        if (player is Player) {
            val buildPlayer = server.asBuildPlayer(player) ?: return@listen
            buildPlayer.cancelWhenBuilder(hangingBreakEvent)
        }
    }

    listen<HangingPlaceEvent> { hangingPlaceEvent ->
        val player = hangingPlaceEvent.player ?: return@listen
        val buildPlayer = server.asBuildPlayer(player) ?: return@listen
        buildPlayer.cancelWhenBuilder(hangingPlaceEvent)
    }

}