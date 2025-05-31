package de.raphaelgoetz.buildLite.command.world.manage

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.buildLite.store.BuildServer
import de.raphaelgoetz.buildLite.store.isWorld
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun getCreateArgument(server: BuildServer): LiteralArgumentBuilder<CommandSourceStack?>? {
    return Commands.literal("create")
        .requires { it.sender.hasPermission("betterbuild.manage.create") }
        .then(
            Commands.argument("name", StringArgumentType.word())
                .executes { ctx ->
                    val player = ctx.source.sender as? Player ?: return@executes 0
                    val world = StringArgumentType.getString(ctx, "name")

                    val clearedName = world.replace(Regex("\\W"), "")
                    val clearedGroup = "unknown" // default value

                    if (world.isWorld()) {
                        player.sendTransText("command.world.manage.create.already-exists") {
                            type = CommunicationType.ERROR
                        }
                        return@executes 0
                    }

                    server.createWorld(clearedName, clearedGroup)
                    player.sendTransText("command.world.manage.create.world-created") {
                        type = CommunicationType.ERROR
                    }
                    1
                }
                .then(
                    Commands.argument("group", StringArgumentType.word())
                        .executes { ctx ->
                            val player = ctx.source.sender as? Player ?: return@executes 0
                            val world = StringArgumentType.getString(ctx, "name")
                            val group = StringArgumentType.getString(ctx, "group")

                            val option = server.worlds.find { it.displayIdentifier == world + "_" + group }

                            if (option != null) {
                                player.sendTransText("command.world.manage.create.already-exists") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            val clearedName = world.replace(Regex("\\W"), "")
                            val clearedGroup = group.replace(Regex("\\W"), "")
                            server.createWorld(clearedName, clearedGroup)
                            player.sendTransText("command.world.manage.create.world-created") {
                                type = CommunicationType.ERROR
                            }
                            1
                        }
                )
        )
}