package de.raphaelgoetz.buildLite.command.server

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.command.brigadier.Commands

fun registerServerManageCommand(server: BuildServer) {
    val command = Commands.literal("server")
        .requires { it.sender.hasPermission("betterbuild.player.physics") }
        .then(Commands.literal("start")
            .executes {
                server.startServer()
                1
            }
        )
        .then(Commands.literal("stop")
            .executes {
                server.stopServer()
                1
            }
        )
        .build()

    registerCommand(
        AstralisCommand(
            command = command,
            description = "Will start or stop the Http Server.",
            aliases = listOf(),
        )
    )
}