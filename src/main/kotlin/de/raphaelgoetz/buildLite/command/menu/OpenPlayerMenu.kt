package de.raphaelgoetz.buildLite.command.menu

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerPlayerMenuCommand() {
    val command = Commands.literal("players").requires { it.sender.hasPermission("build-lite.command.menu.players") }
        .executes { context ->
            val player = context.source.sender as? Player ?: return@executes 0
            player.showHomeDialog()
            1
        }.build()

    registerCommand(
        AstralisCommand(
            command = command, description = "Opens the players menu.", aliases = listOf()
        )
    )
}