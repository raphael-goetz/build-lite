package de.raphaelgoetz.buildLite.command.player

import com.mojang.brigadier.arguments.StringArgumentType
import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.GameMode
import org.bukkit.entity.Player

private val GAME_MODE_ALIASES = mapOf(
    "0" to GameMode.SURVIVAL, "survival" to GameMode.SURVIVAL,
    "1" to GameMode.CREATIVE, "creative" to GameMode.CREATIVE,
    "2" to GameMode.ADVENTURE, "adventure" to GameMode.ADVENTURE,
    "3" to GameMode.SPECTATOR, "spectator" to GameMode.SPECTATOR,
)

fun registerGameModeCommand() {
    val gameModeCommand = Commands.literal("gm")
        .requires { it.sender.hasPermission("betterbuild.player.gamemode") }
        .then(
            Commands.argument("mode", StringArgumentType.word())
                .suggests { _, builder ->
                    GAME_MODE_ALIASES.keys.distinct().forEach { builder.suggest(it) }
                    builder.buildFuture()
                }
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes 0
                    val input = StringArgumentType.getString(context, "mode").lowercase()

                    val gameMode = GAME_MODE_ALIASES[input]
                    if (gameMode == null) {
                        player.sendTransText("command.player.gamemode.error") {
                            type = CommunicationType.ERROR
                        }
                        return@executes 0
                    }

                    player.gameMode = gameMode
                    player.sendTransText("command.player.gamemode.success") {
                        type = CommunicationType.SUCCESS
                    }
                    1
                }
        )
        .build()

    registerCommand(
        AstralisCommand(
            command = gameModeCommand,
            description = "This is a shortcut for the gamemode command.",
            aliases = emptyList()
        )
    )
}
