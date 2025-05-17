package de.raphaelgoetz.buildLite.command.world

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerPhysicsCommand(server: BuildServer) {
    val command = Commands.literal("physics")
        .requires { it.sender.hasPermission("betterbuild.player.physics") }
        .executes { context ->

            val player = context.source.sender as? Player ?: return@executes 0
            val buildWorld = server.asBuildWorld(player.world) ?: return@executes 0

            buildWorld.togglePhysics()
            1
        }
        .build()

    registerCommand(
        AstralisCommand(
            command = command,
            description = "Will teleport the user to the last location in the last world he visited.",
            aliases = listOf("ph", "gravity"),
        )
    )
}