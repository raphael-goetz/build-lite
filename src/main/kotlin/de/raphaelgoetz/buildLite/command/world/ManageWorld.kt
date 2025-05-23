package de.raphaelgoetz.buildLite.command.world

import com.mojang.brigadier.arguments.StringArgumentType
import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.astralis.world.existingWorlds
import de.raphaelgoetz.buildLite.menu.openWorldDeleteMenu
import de.raphaelgoetz.buildLite.record.WorldState
import de.raphaelgoetz.buildLite.store.BuildServer
import de.raphaelgoetz.buildLite.store.isWorld
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerManageWorldCommand(server: BuildServer) {

    val createArgument = Commands.literal("create")
        .requires { it.sender.hasPermission("betterbuild.manage.create") }
        .then(
            Commands.argument("name", StringArgumentType.word())
                .executes { ctx ->
                    val player = ctx.source.sender as? Player ?: return@executes 0
                    val world = StringArgumentType.getString(ctx, "name")

                    val clearedName = world.replace(Regex("\\W"), "")
                    val clearedGroup = "unknown" // default value

                    if (world.isWorld()) {
                        player.sendTransText("command.world.manage.exists") {
                            type = CommunicationType.ERROR
                        }
                        return@executes 0
                    }

                    server.createWorld(clearedName, clearedGroup)
                    1
                }
                .then(
                    Commands.argument("group", StringArgumentType.word())
                        .executes { ctx ->
                            val player = ctx.source.sender as? Player ?: return@executes 0
                            val world = StringArgumentType.getString(ctx, "name")
                            val group = StringArgumentType.getString(ctx, "group")

                            if (world.isWorld()) {
                                player.sendTransText("command.world.manage.exists") {
                                    type = CommunicationType.ERROR
                                }
                                return@executes 0
                            }

                            val clearedName = world.replace(Regex("\\W"), "")
                            val clearedGroup = group.replace(Regex("\\W"), "")
                            server.createWorld(clearedName, clearedGroup)
                            1
                        }
                )
        )

    val migrateArgument = Commands.literal("migrate")
        .then(
            Commands.argument("world", StringArgumentType.word())
                .suggests { _, builder ->
                    server.migrateWorlds.forEach { builder.suggest(it.toString()) }
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
                                                player.sendTransText("command.world.migrate.type.error") {
                                                    type = CommunicationType.ERROR
                                                }
                                                return@executes 0
                                            }

                                            if (!server.migrateWorlds.contains(world)) {
                                                player.sendTransText("command.world.manage.exists") {
                                                    type = CommunicationType.ERROR
                                                }
                                                return@executes 0
                                            }

                                            val clearedName = name.replace(Regex("\\W"), "")
                                            val clearedGroup = group.replace(Regex("\\W"), "")

                                            server.migrateWorld(world, clearedName, clearedGroup, state)
                                            1
                                        }
                                )
                        )
                )
        )


    val deleteArgument = Commands.literal("delete")
        .then(Commands.argument("world", StringArgumentType.word())
            .suggests { _, builder ->
                existingWorlds.forEach { builder.suggest(it) }
                builder.buildFuture()
            }
            .executes { ctx ->
                val player = ctx.source.sender as? Player ?: return@executes 0
                val world = StringArgumentType.getString(ctx, "world")

                val buildPlayer = server.asBuildPlayer(player) ?: return@executes 0
                val buildWorld = server.asBuildWorld(world) ?: return@executes 0

                if (!world.isWorld()) {
                    player.sendTransText("command.world.manage.unknown") {
                        type = CommunicationType.ERROR
                    }
                    return@executes 0
                }

                buildPlayer.openWorldDeleteMenu(server, buildWorld)
                1
            }
        )

    val updateSpawnArguemnt = Commands.literal("spawn")
        .executes { ctx ->
            val player = ctx.source.sender as? Player ?: return@executes 0
            player.world.spawnLocation  = player.location

            player.sendTransText("command.world.spawn.changed") {
                type = CommunicationType.SUCCESS
            }
            1
        }

   val manageCommand = Commands.literal("world")
        .requires { it.sender is Player }
        .then(createArgument)
        .then(migrateArgument)
        .then(deleteArgument)
        .then(updateSpawnArguemnt)
        .build()

    registerCommand(
        AstralisCommand(
            command = manageCommand,
            description = "Manage building worlds (create, delete, spawn)",
            aliases = listOf("w", "manage")
        )
    )

}