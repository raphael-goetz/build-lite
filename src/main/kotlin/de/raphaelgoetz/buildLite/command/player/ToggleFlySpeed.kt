package de.raphaelgoetz.buildLite.command.player

import com.mojang.brigadier.arguments.FloatArgumentType
import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerFlySpeedCommand() {
    val velocities = listOf(
        0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0
    )

    val flySpeedCommand = Commands.literal("fly")
        .requires { it.sender.hasPermission("betterbuild.player.fly") }
        .then(
            Commands.argument("velocity", FloatArgumentType.floatArg(0.0f, 1.0f))
                .suggests { _, builder ->
                    velocities.forEach { builder.suggest(it.toString()) }
                    builder.buildFuture()
                }
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes 0
                    val velocity = FloatArgumentType.getFloat(context, "velocity")

                    player.flySpeed = velocity
                    1
               }
        )
        .build()

    registerCommand(
        AstralisCommand(
            command = flySpeedCommand,
            description = "Toggles the fly speed of a user.",
            aliases = listOf("flyspeed", "speed"),
        )
    )
}
