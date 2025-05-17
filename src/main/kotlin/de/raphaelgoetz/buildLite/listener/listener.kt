package de.raphaelgoetz.buildLite.listener

import de.raphaelgoetz.buildLite.listener.minecraft.registerBlockEvents
import de.raphaelgoetz.buildLite.listener.minecraft.registerEntityEvents
import de.raphaelgoetz.buildLite.listener.minecraft.registerHangingEvents
import de.raphaelgoetz.buildLite.listener.minecraft.registerPlayerEvents
import de.raphaelgoetz.buildLite.listener.minecraft.registerRaidEvents
import de.raphaelgoetz.buildLite.listener.minecraft.registerVehicleEvents
import de.raphaelgoetz.buildLite.listener.minecraft.registerWorldEvents
import de.raphaelgoetz.buildLite.store.BuildServer

fun BuildServer.registerListener() {

    registerBlockEvents(this)
    registerEntityEvents()
    registerHangingEvents(this)
    registerPlayerEvents(this)
    registerRaidEvents()
    registerVehicleEvents()
    registerWorldEvents(this)
}
