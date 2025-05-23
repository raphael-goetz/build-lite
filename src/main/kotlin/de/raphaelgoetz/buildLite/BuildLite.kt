package de.raphaelgoetz.buildLite

import de.raphaelgoetz.astralis.Astralis
import de.raphaelgoetz.buildLite.command.registerCommands
import de.raphaelgoetz.buildLite.listener.registerListener
import de.raphaelgoetz.buildLite.store.BuildServer

class BuildLite : Astralis() {
    override fun enable() {
        val server = BuildServer()
        server.registerCommands()
        server.registerListener()

        server.migrateWorlds.forEach {
            println("migrate $it")
        }
    }
}

