package de.raphaelgoetz.buildLite

import com.sun.net.httpserver.HttpExchange
import de.raphaelgoetz.astralis.Astralis
import de.raphaelgoetz.buildLite.command.registerCommands
import de.raphaelgoetz.buildLite.listener.registerListener
import de.raphaelgoetz.buildLite.store.BuildServer

class BuildLite : Astralis() {
    override fun enable() {
        val buildServer = BuildServer()
        buildServer.registerCommands()
        buildServer.registerListener()
    }
}

