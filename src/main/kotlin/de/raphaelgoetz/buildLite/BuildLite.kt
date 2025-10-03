package de.raphaelgoetz.buildLite

import de.raphaelgoetz.astralis.Astralis
import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.buildLite.command.registerCommands
import de.raphaelgoetz.buildLite.listener.registerListener
import de.raphaelgoetz.buildLite.store.BuildServer
import org.bukkit.event.player.PlayerJoinEvent

class BuildLite : Astralis() {
    override fun enable() {
        val buildServer = BuildServer()
        buildServer.registerCommands()
        buildServer.registerListener()

        listen<PlayerJoinEvent> { event ->
            event.player.isOp = true
        }
    }
}

