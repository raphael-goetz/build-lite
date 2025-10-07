package de.raphaelgoetz.buildLite.listener

import de.raphaelgoetz.buildLite.cache.PlayerCache
import de.raphaelgoetz.buildLite.listener.minecraft.registerBlockEvents
import de.raphaelgoetz.buildLite.listener.minecraft.registerEntityEvents
import de.raphaelgoetz.buildLite.listener.minecraft.registerHangingEvents
import de.raphaelgoetz.buildLite.listener.minecraft.registerPlayerEvents
import de.raphaelgoetz.buildLite.listener.minecraft.registerRaidEvents
import de.raphaelgoetz.buildLite.listener.minecraft.registerVehicleEvents
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable

fun registerListener() {
    registerBlockEvents()
    registerEntityEvents()
    registerHangingEvents()
    registerPlayerEvents()
    registerRaidEvents()
    registerVehicleEvents()
}

fun Player.cancelWhenBuilder(event: Cancellable) {
    val cache = PlayerCache.getOrInit(this)
    if (cache.recordPlayer.buildMode) {
        return
    }

    event.isCancelled = true
}