package de.raphaelgoetz.buildLite.command

import de.raphaelgoetz.buildLite.command.menu.registerBannerMenuCommand
import de.raphaelgoetz.buildLite.command.menu.registerPlayerMenuCommand
import de.raphaelgoetz.buildLite.command.menu.registerWarpMenuCommand
import de.raphaelgoetz.buildLite.command.menu.registerWorldMenuCommand

fun registerCommands() {
    registerBannerMenuCommand()
    registerPlayerMenuCommand()
    registerPlayerMenuCommand()
    registerWarpMenuCommand()
    registerWorldMenuCommand()
}