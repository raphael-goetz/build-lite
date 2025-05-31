package de.raphaelgoetz.buildLite.command

import de.raphaelgoetz.buildLite.command.player.registerBuildCommand
import de.raphaelgoetz.buildLite.command.player.registerDarkCommand
import de.raphaelgoetz.buildLite.command.player.registerFlySpeedCommand
import de.raphaelgoetz.buildLite.command.player.registerGameModeCommand
import de.raphaelgoetz.buildLite.command.player.registerManageWarpsCommand
import de.raphaelgoetz.buildLite.command.player.registerToLastLocationCommand
import de.raphaelgoetz.buildLite.command.server.registerServerManageCommand
import de.raphaelgoetz.buildLite.command.world.registerManageWorldCommand
import de.raphaelgoetz.buildLite.command.world.registerPhysicsCommand
import de.raphaelgoetz.buildLite.store.BuildServer

fun BuildServer.registerCommands() {
    //Player commands
    registerManageWarpsCommand(this)
    registerGameModeCommand()
    registerBuildCommand(this)
    registerDarkCommand(this)
    registerFlySpeedCommand()
    registerToLastLocationCommand(this)

    //World commands
    registerPhysicsCommand(this)
    registerManageWorldCommand(this)

    //Server commands
    registerServerManageCommand(this)
}