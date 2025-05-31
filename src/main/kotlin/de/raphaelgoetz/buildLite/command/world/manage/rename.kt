package de.raphaelgoetz.buildLite.command.world.manage

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun getRenameArgument(server: BuildServer): LiteralArgumentBuilder<CommandSourceStack?>? {
    return Commands.literal("rename")
        .requires { it.sender.hasPermission("betterbuild.manage.rename") }
        .then(
            Commands.argument("world", StringArgumentType.word())
                .suggests { _, builder ->
                    server.worlds.forEach { builder.suggest(it.displayIdentifier) }
                    builder.buildFuture()
                }
                .then(Commands.literal("name")
                    .then(Commands.argument("newName", StringArgumentType.word())
                        .executes { ctx ->
                            val player = ctx.source.sender as? Player ?: return@executes 0
                            val world = StringArgumentType.getString(ctx, "world")
                            val buildWorld = server.byDisplayIdentifier(world) ?: return@executes 0
                            val newName = StringArgumentType.getString(ctx, "newName")

                            val option = server.worlds.find { it.name == newName }

                            if (option != null) {
                                player.sendTransText("command.world.manage.rename.world-name-not-available") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            buildWorld.updateName(newName)
                            player.sendTransText("command.world.manage.rename.world-name-changed") {
                                type = CommunicationType.SUCCESS
                            }

                            server.refetchWorlds()
                            1
                        }
                    )
                )
                .then(Commands.literal("group")
                    .then(Commands.argument("newGroup", StringArgumentType.word())
                        .executes { ctx ->
                            val player = ctx.source.sender as? Player ?: return@executes 0
                            val world = StringArgumentType.getString(ctx, "world")
                            val buildWorld = server.byDisplayIdentifier(world) ?: return@executes 0
                            val newGroup = StringArgumentType.getString(ctx, "newGroup")

                            val option = server.worlds.find { it.group == newGroup }
                            if (option != null) {
                                player.sendTransText("command.world.manage.rename.world-group-not-available") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            buildWorld.updateGroup(newGroup)
                            player.sendTransText("command.world.manage.rename.world-group-changed") {
                                type = CommunicationType.SUCCESS
                            }

                            server.refetchWorlds()

                            1
                        }
                    )
                )
        )
}