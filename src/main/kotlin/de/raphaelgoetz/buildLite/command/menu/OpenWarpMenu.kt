package de.raphaelgoetz.buildLite.command.menu

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.menu.openWarpMenu
import de.raphaelgoetz.buildLite.player.getCurrentWorldUUID
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerWarpMenuCommand() {
    val command = Commands.literal("warps").requires { it.sender.hasPermission("build-lite.command.menu.warps") }
        .executes { context ->
            val player = context.source.sender as? Player ?: return@executes 0
            val uuid = player.getCurrentWorldUUID() ?: return@executes 0
            player.openWarpMenu(uuid)
            1
        }.build()

    registerCommand(
        AstralisCommand(
            command = command, description = "Opens the warps menu of the world you are in.", aliases = listOf()
        )
    )
}