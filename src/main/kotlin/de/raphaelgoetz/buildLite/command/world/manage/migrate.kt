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

fun getMigrateArgument(server: BuildServer): LiteralArgumentBuilder<CommandSourceStack?>? {
    return Commands.literal("migrate")
        .requires { it.sender.hasPermission("betterbuild.manage.migrate") }
        .then(
            Commands.argument("world", StringArgumentType.word())
                .suggests { _, builder ->
                    server.migrateWorlds.forEach { builder.suggest(it) }
                    builder.buildFuture()
                }
                .then(
                    Commands.argument("name", StringArgumentType.word())
                        .then(
                            Commands.argument("group", StringArgumentType.word())
                                .then(
                                    Commands.argument("state", StringArgumentType.word())
                                        .suggests { _, builder ->
                                            WorldState.entries.forEach { builder.suggest(it.toString()) }
                                            builder.buildFuture()
                                        }
                                        .executes { ctx ->
                                            val player = ctx.source.sender as? Player ?: return@executes 0
                                            val world = StringArgumentType.getString(ctx, "world")
                                            val name = StringArgumentType.getString(ctx, "name")
                                            val group = StringArgumentType.getString(ctx, "group")
                                            val stateStr = StringArgumentType.getString(ctx, "state")

                                            val state = try {
                                                WorldState.valueOf(stateStr.uppercase())
                                            } catch (_: IllegalArgumentException) {
                                                player.sendTransText("command.world.manage.migrate.type.error") {
                                                    type = CommunicationType.ERROR
                                                }
                                                return@executes 0
                                            }

                                            if (!server.migrateWorlds.contains(world)) {
                                                player.sendTransText("command.world.manage.migrate.no-world") {
                                                    type = CommunicationType.ERROR
                                                }
                                                return@executes 0
                                            }

                                            val clearedName = name.replace(Regex("\\W"), "")
                                            val clearedGroup = group.replace(Regex("\\W"), "")

                                            server.migrateWorld(world, clearedName, clearedGroup, state)
                                            player.sendTransText("command.world.manage.migrate.migrated") {
                                                type = CommunicationType.SUCCESS
                                            }

                                            1
                                        }
                                )
                        )
                )
        )
}