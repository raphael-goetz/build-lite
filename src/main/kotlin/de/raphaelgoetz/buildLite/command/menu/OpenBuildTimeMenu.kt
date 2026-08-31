package de.raphaelgoetz.buildLite.command.menu

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.menu.openBuildTimePlayerPickerMenu
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerBuildTimeMenuCommand() {
    val command =
        Commands.literal("buildtime").requires {
            it.sender.hasPermission("build-lite.buildtime.admin") || it.sender.hasPermission("build-lite.*")
        }
            .executes { context ->
                val player = context.source.sender as? Player ?: return@executes 0
                player.openBuildTimePlayerPickerMenu()
                1
            }.build()

    registerCommand(
        AstralisCommand(
            command = command, description = "Opens the build time admin menu.", aliases = listOf()
        )
    )
}
