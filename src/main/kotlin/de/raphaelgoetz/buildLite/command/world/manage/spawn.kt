package de.raphaelgoetz.buildLite.command.world.manage

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun getSpawnArgument(server: BuildServer): LiteralArgumentBuilder<CommandSourceStack?>? {
    return Commands.literal("spawn")
            .executes { ctx ->
                val player = ctx.source.sender as? Player ?: return@executes 0
                val buildWorld = server.asBuildWorld(player.world)

                if (buildWorld == null) {
                    player.sendTransText("command.world.manage.spawn.no-world") {
                        type = CommunicationType.ERROR
                    }
                    return@executes 0
                }

                buildWorld.updateSpawn(player.location)
                server.refetchWorlds()

                player.sendTransText("command.world.manage.spawn.changed") {
                    type = CommunicationType.SUCCESS
                }

                1
        }
}