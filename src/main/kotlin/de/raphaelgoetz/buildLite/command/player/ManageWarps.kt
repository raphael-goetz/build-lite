package de.raphaelgoetz.buildLite.command.player

import com.mojang.brigadier.arguments.StringArgumentType
import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.menu.openWarpMenu
import de.raphaelgoetz.buildLite.record.createPrivateWarp
import de.raphaelgoetz.buildLite.record.createPublicWarp
import de.raphaelgoetz.buildLite.record.deleteWarp
import de.raphaelgoetz.buildLite.record.getGlobalWarps
import de.raphaelgoetz.buildLite.record.getPrivateWarp
import de.raphaelgoetz.buildLite.record.getPrivateWarps
import de.raphaelgoetz.buildLite.record.getPublicWarp
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerManageWarpsCommand(server: BuildServer) {

    val command = Commands.literal("warp")
        .requires { it.sender.hasPermission("betterbuild.player.warps") }
        .then(
            Commands.literal("public")
                .then(
                    Commands.literal("create")
                        .then(
                            Commands.argument("name", StringArgumentType.word())
                                .executes { ctx ->
                                    val player = ctx.source.sender as? Player ?: return@executes 0
                                    val buildPlayer = server.asBuildPlayer(player) ?: return@executes 0
                                    val name = StringArgumentType.getString(ctx, "name")
                                    val clearedName = name.replace(Regex("\\W"), "")

                                    buildPlayer.createPublicWarp(server, clearedName)
                                    1
                                }
                        )
                )
                .then(
                    Commands.literal("delete")
                        .then(
                            Commands.argument("name", StringArgumentType.word())
                                .suggests { _, builder ->
                                    getGlobalWarps().forEach { builder.suggest(it.name) }
                                    builder.buildFuture()
                                }
                                .executes { ctx ->
                                    val name = StringArgumentType.getString(ctx, "name")
                                    val warp = getPublicWarp(name) ?: return@executes 0
                                    deleteWarp(warp.id)
                                    1
                                }
                        )
                )
                .then(
                    Commands.literal("info")
                        .executes { ctx ->
                            val player = ctx.source.sender as? Player ?: return@executes 0
                            val buildPlayer = server.asBuildPlayer(player) ?: return@executes 0

                            val warps = getGlobalWarps()
                            println(warps.toString())
                            buildPlayer.openWarpMenu(warps, server)
                            1
                        }
                )
        )
        .then(
            Commands.literal("private")
                .then(
                    Commands.literal("create")
                        .then(
                            Commands.argument("name", StringArgumentType.word())
                                .executes { ctx ->
                                    val player = ctx.source.sender as? Player ?: return@executes 0
                                    val buildPlayer = server.asBuildPlayer(player) ?: return@executes 0
                                    val name = StringArgumentType.getString(ctx, "name")
                                    val clearedName = name.replace(Regex("\\W"), "")

                                    buildPlayer.createPrivateWarp(server, clearedName)
                                    1
                                }
                        )
                )
                .then(
                    Commands.literal("delete")
                        .then(
                            Commands.argument("name", StringArgumentType.word())
                                .suggests { ctx, builder ->
                                    val player = ctx.source.sender as? Player ?: return@suggests null
                                    val buildPlayer = server.asBuildPlayer(player) ?: return@suggests null

                                    buildPlayer.getPrivateWarps().forEach { builder.suggest(it.name) }
                                    builder.buildFuture()
                                }
                                .executes { ctx ->
                                    val player = ctx.source.sender as? Player ?: return@executes 0
                                    val name = StringArgumentType.getString(ctx, "name")
                                    val warp = getPrivateWarp(player.uniqueId, name) ?: return@executes 0
                                    deleteWarp(warp.id)
                                    1
                                }
                        )
                )
                .then(
                    Commands.literal("info")
                        .executes { ctx ->
                            val player = ctx.source.sender as? Player ?: return@executes 0
                            val buildPlayer = server.asBuildPlayer(player) ?: return@executes 0

                            val warps = buildPlayer.getPrivateWarps()
                            buildPlayer.openWarpMenu(warps, server)
                            1
                        }
                )
        )
        .build()

    registerCommand(
        AstralisCommand(
            command = command,
            description = "Lets you create & delete warps",
            aliases = listOf()
        )
    )
}