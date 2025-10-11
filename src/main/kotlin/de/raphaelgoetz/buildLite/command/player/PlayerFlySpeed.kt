package de.raphaelgoetz.buildLite.command.player

import com.mojang.brigadier.arguments.IntegerArgumentType
import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.astralis.text.sendText
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.PREFIX
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerFlySpeedCommand() {
    val velocities = listOf(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
    )

    val flySpeedCommand = Commands.literal("fly")
        .requires { it.sender.hasPermission("build-lite.command.player.fly") }
        .then(
            Commands.argument("velocity", IntegerArgumentType.integer(0, 10))
                .suggests { _, builder ->
                    velocities.forEach { builder.suggest(it.toString()) }
                    builder.buildFuture()
                }
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes 0
                    val velocity = IntegerArgumentType.getInteger(context, "velocity") * 0.1f

                    player.sendText("$PREFIX Your fly speed has been updated to $velocity.") {
                        color = Colorization.LIME
                    }

                    player.flySpeed = velocity
                    1
                }
        )
        .build()

    registerCommand(
        AstralisCommand(
            command = flySpeedCommand,
            description = "Set the velocity of your fly speed (arguments from 0-10).",
            aliases = listOf("flyspeed", "speed"),
        )
    )
}