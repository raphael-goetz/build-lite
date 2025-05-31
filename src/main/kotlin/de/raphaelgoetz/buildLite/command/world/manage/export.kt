package de.raphaelgoetz.buildLite.command.world.manage

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun getExportArgument(server: BuildServer): LiteralArgumentBuilder<CommandSourceStack?>? {
    return Commands.literal("export")
        .requires { it.sender.hasPermission("betterbuild.manage.export") }
        .then(Commands.argument("world", StringArgumentType.word())
            .suggests { _, builder ->
                server.worlds.forEach { builder.suggest(it.displayIdentifier) }
                builder.buildFuture()
            }
            .executes { ctx ->
                val player = ctx.source.sender as? Player ?: return@executes 0
                val world = StringArgumentType.getString(ctx, "world")
                val buildWorld = server.byDisplayIdentifier(world)

                if (buildWorld == null) {
                    player.sendTransText("command.world.manage.export.empty-world") {
                        type = CommunicationType.ERROR
                    }
                    return@executes 0
                }

                server.exportWorld(buildWorld)
                player.sendTransText("command.world.manage.export.success") {
                    type = CommunicationType.SUCCESS
                }

                1
            }
        )
}