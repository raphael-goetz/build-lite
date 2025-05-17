package de.raphaelgoetz.buildLite.command.player

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerToLastLocationCommand(server: BuildServer) {
    val command = Commands.literal("back")
        .requires { it.sender.hasPermission("betterbuild.player.back") }
        .executes { context ->

            val player = context.source.sender as? Player ?: return@executes 0
            val buildPlayer = server.asBuildPlayer(player) ?: return@executes 0

            buildPlayer.teleportToLastLocation()

            1
        }
        .build()

    registerCommand(
        AstralisCommand(
            command = command,
            description = "Will teleport the user to the last location in the last world he visited.",
            aliases = emptyList(),
        )
    )
}