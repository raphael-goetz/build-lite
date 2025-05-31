package de.raphaelgoetz.buildLite.command.world.manage

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.buildLite.menu.openCreditMenu
import de.raphaelgoetz.buildLite.record.addCredit
import de.raphaelgoetz.buildLite.record.removeCredit
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun getCreditArgument(server: BuildServer): LiteralArgumentBuilder<CommandSourceStack?>? {
    return Commands.literal("credit")
        .requires { it.sender.hasPermission("betterbuild.manage.credit") }
        .then(
        Commands.argument("world", StringArgumentType.word())
                .suggests { _, builder ->
                    server.worlds.forEach { builder.suggest(it.displayIdentifier) }
                    builder.buildFuture()
                }
                .then(Commands.literal("add")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .executes { ctx ->
                            val player = ctx.source.sender as? Player ?: return@executes 0
                            val playerName = StringArgumentType.getString(ctx, "player")

                            if (playerName == null || playerName.isEmpty()) {
                                player.sendTransText("command.world.manage.credit.empty-player-name") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            val worldName = StringArgumentType.getString(ctx, "world")

                            if (worldName == null || worldName.isEmpty()) {
                                player.sendTransText("command.world.manage.credit.empty-world") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            val world = server.byDisplayIdentifier(worldName)

                            if (world == null) {
                                player.sendTransText("command.world.manage.credit.empty-world") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            val offlinePlayer = Bukkit.getOfflinePlayer(playerName)
                            world.addCredit(offlinePlayer.uniqueId)
                            player.sendTransText("command.world.manage.credit.created") {
                                type = CommunicationType.SUCCESS
                            }

                            1
                        }
                    )
                )
                .then(Commands.literal("remove")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .executes { ctx ->
                            val player = ctx.source.sender as? Player ?: return@executes 0
                            val playerName = StringArgumentType.getString(ctx, "player")

                            if (playerName == null || playerName.isEmpty()) {
                                player.sendTransText("command.world.manage.credit.empty-player-name") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            val worldName = StringArgumentType.getString(ctx, "world")

                            if (worldName == null || worldName.isEmpty()) {
                                player.sendTransText("command.world.manage.credit.empty-world") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            val world = server.byDisplayIdentifier(worldName)

                            if (world == null) {
                                player.sendTransText("command.world.manage.credit.empty-world") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            val offlinePlayer = Bukkit.getOfflinePlayer(playerName)
                            world.removeCredit(offlinePlayer.uniqueId)
                            player.sendTransText("command.world.manage.credit.removed") {
                                type = CommunicationType.SUCCESS
                            }

                            1
                        }
                    )
                )
                .then(Commands.literal("info")
                    .executes { ctx ->
                        val player = ctx.source.sender as? Player ?: return@executes 0
                        val worldName = StringArgumentType.getString(ctx, "world")
                        val world = server.byDisplayIdentifier(worldName) ?: return@executes 0

                        player.sendTransText("command.world.manage.credit.loading") {
                            type = CommunicationType.SUCCESS
                        }

                        player.openCreditMenu(world)
                        1
                    }
                )
        )
}