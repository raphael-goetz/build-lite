package de.raphaelgoetz.buildLite.command.menu

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.menu.openBannerCreationMenu
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerBannerMenuCommand() {
    val command = Commands.literal("banner").requires { it.sender.hasPermission("build-lite.command.menu.banner") }
        .executes { context ->
            val player = context.source.sender as? Player ?: return@executes 0
            player.openBannerCreationMenu()
            1
        }.build()

    registerCommand(
        AstralisCommand(
            command = command, description = "Opens the banner creation menu.", aliases = listOf()
        )
    )
}