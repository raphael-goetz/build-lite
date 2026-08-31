package de.raphaelgoetz.buildLite.command

import de.raphaelgoetz.buildLite.command.menu.registerBannerMenuCommand
import de.raphaelgoetz.buildLite.command.menu.registerBuildTimeMenuCommand
import de.raphaelgoetz.buildLite.command.menu.registerPlayerMenuCommand
import de.raphaelgoetz.buildLite.command.menu.registerReviewMenuCommand
import de.raphaelgoetz.buildLite.command.menu.registerWarpMenuCommand
import de.raphaelgoetz.buildLite.command.menu.registerWorldMenuCommand
import de.raphaelgoetz.buildLite.command.player.registerFlySpeedCommand

fun registerCommands() {
    registerBuildLiteCommand()
    registerBannerMenuCommand()
    registerBuildTimeMenuCommand()
    registerPlayerMenuCommand()
    registerReviewMenuCommand()
    registerWarpMenuCommand()
    registerWorldMenuCommand()
    registerFlySpeedCommand()
}
