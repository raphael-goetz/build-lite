package de.raphaelgoetz.buildLite.command.world.manage

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.buildLite.record.WorldState
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun getStateArgument(server: BuildServer): LiteralArgumentBuilder<CommandSourceStack?>? {
    return Commands.literal("state")
        .requires { it.sender.hasPermission("betterbuild.manage.state") }
        .then(
            Commands.argument("world", StringArgumentType.word())
                .suggests { _, builder ->
                    server.worlds.forEach { builder.suggest(it.displayIdentifier) }
                    builder.buildFuture()
                }
                .then(
                    Commands.argument("state", StringArgumentType.word())
                        .suggests { _, builder ->
                            WorldState.entries.forEach { builder.suggest(it.toString()) }
                            builder.buildFuture()
                        }
                        .executes { ctx ->
                            val player = ctx.source.sender as? Player ?: return@executes 0
                            val world = StringArgumentType.getString(ctx, "world")
                            val stateStr = StringArgumentType.getString(ctx, "state")

                            val state = try {
                                WorldState.valueOf(stateStr.uppercase())
                            } catch (_: IllegalArgumentException) {

                                player.sendTransText("command.world.migrate.type.error") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            val buildWorld = server.byDisplayIdentifier(world)

                            if (buildWorld == null) {
                                player.sendTransText("command.world.manage.state.empty-world") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            buildWorld.updateState(state)
                            player.sendTransText("command.world.manage.state.updated") {
                                type = CommunicationType.ERROR
                            }

                            server.refetchWorlds()

                            1
                        }
                )
        )
}