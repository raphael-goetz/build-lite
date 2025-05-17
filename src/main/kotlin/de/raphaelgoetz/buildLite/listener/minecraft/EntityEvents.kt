package de.raphaelgoetz.buildLite.listener.minecraft

import de.raphaelgoetz.astralis.event.listen
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.entity.EntitySpawnEvent

fun registerEntityEvents() {
    listen<EntitySpawnEvent> { event ->
        val entity = event.entity

        if (entity is TNTPrimed) {
            event.isCancelled = true
            event.entity.remove()
        }
    }
}