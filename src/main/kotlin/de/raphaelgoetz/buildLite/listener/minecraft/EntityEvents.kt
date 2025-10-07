package de.raphaelgoetz.buildLite.listener.minecraft

import de.raphaelgoetz.astralis.event.listen
import org.bukkit.entity.Creeper
import org.bukkit.entity.EnderCrystal
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Fireball
import org.bukkit.entity.Ghast
import org.bukkit.entity.TNTPrimed
import org.bukkit.entity.Wither
import org.bukkit.event.entity.EntitySpawnEvent

fun registerEntityEvents() {
    listen<EntitySpawnEvent> { event ->
        val entity = event.entity

        if (entity is TNTPrimed) {
            event.isCancelled = true
            event.entity.remove()
        }

        if (entity is Fireball || entity is Wither || entity is Ghast || entity is EnderDragon || entity is Creeper || entity is EnderCrystal) {
            event.isCancelled = true
            event.entity.remove()
        }
    }
}