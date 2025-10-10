package de.raphaelgoetz.buildLite.command.menu

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.menu.openReviewMenu
import de.raphaelgoetz.buildLite.player.getCurrentWorldUUID
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerReviewMenuCommand() {
    val command = Commands.literal("reviews").requires { it.sender.hasPermission("build-lite.command.menu.reviews") }
        .executes { context ->
            val player = context.source.sender as? Player ?: return@executes 0
            val uuid = player.getCurrentWorldUUID() ?: return@executes 0
            player.openReviewMenu(uuid)
            1
        }.build()

    registerCommand(
        AstralisCommand(
            command = command, description = "Opens the review menu of the world you are in.", aliases = listOf()
        )
    )
}