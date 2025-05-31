package de.raphaelgoetz.buildLite.command.world

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.buildLite.command.world.manage.getCreateArgument
import de.raphaelgoetz.buildLite.command.world.manage.getCreditArgument
import de.raphaelgoetz.buildLite.command.world.manage.getDeleteArgument
import de.raphaelgoetz.buildLite.command.world.manage.getExportArgument
import de.raphaelgoetz.buildLite.command.world.manage.getMigrateArgument
import de.raphaelgoetz.buildLite.command.world.manage.getRenameArgument
import de.raphaelgoetz.buildLite.command.world.manage.getSpawnArgument
import de.raphaelgoetz.buildLite.command.world.manage.getStateArgument
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerManageWorldCommand(server: BuildServer) {

   val manageCommand = Commands.literal("world")
        .requires { it.sender is Player }
        .then(getCreateArgument(server))
        .then(getCreditArgument(server))
        .then(getDeleteArgument(server))
        .then(getExportArgument(server))
        .then(getMigrateArgument(server))
        .then(getRenameArgument(server))
        .then(getSpawnArgument(server))
        .then(getStateArgument(server))
        .build()

    registerCommand(
        AstralisCommand(
            command = manageCommand,
            description = "Manage building worlds (create, delete, spawn)",
            aliases = listOf("w", "manage")
        )
    )
}