package de.raphaelgoetz.buildLite.command.player

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerBuildCommand(server: BuildServer) {
    val buildModeCommand = Commands.literal("build")
        .requires { it.sender.hasPermission("betterbuild.player.build") }
        .executes { context ->
            val player = context.source.sender as? Player ?: return@executes 0
            val buildPlayer = server.asBuildPlayer(player) ?: return@executes 0

            buildPlayer.toggleBuildMode()

            1
        }
        .build()

    registerCommand(
        AstralisCommand(
            command = buildModeCommand,
            description = "Toggles whether a user can build or not.",
            aliases = emptyList()
        )
    )
}
