package de.raphaelgoetz.buildLite.listener.minecraft

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.buildLite.store.BuildServer
import org.bukkit.event.world.WorldLoadEvent

fun registerWorldEvents(server: BuildServer) {

    listen<WorldLoadEvent> { worldLoadEvent ->
        server.reloadWorlds()
        val eventWorld = worldLoadEvent.world
        server.completeQueue(eventWorld)
    }

}