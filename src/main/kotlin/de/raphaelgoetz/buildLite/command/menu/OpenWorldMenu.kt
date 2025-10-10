package de.raphaelgoetz.buildLite.command.menu

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.menu.openWorldFolderMenu
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerWorldMenuCommand() {
    val command = Commands.literal("worlds").requires { it.sender.hasPermission("build-lite.command.menu.worlds") }
        .executes { context ->
            val player = context.source.sender as? Player ?: return@executes 0
            player.openWorldFolderMenu()
            1
        }.build()

    registerCommand(
        AstralisCommand(
            command = command, description = "Opens the world menu.", aliases = listOf()
        )
    )
}