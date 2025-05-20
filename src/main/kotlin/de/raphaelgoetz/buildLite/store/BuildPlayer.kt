package de.raphaelgoetz.buildLite.store

import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BuildPlayer(val player: Player, var isBuilding: Boolean = false,  var lastLocation: Location? = null) {

    var isDarkMode: Boolean = hasActiveNightVision()

    fun updateLastLocation(location: Location) {
        lastLocation = location
    }

    fun teleportToLastLocation() {
        if (lastLocation == null) {
            player.sendTransText("store.player.teleport.undefined") {
                type = CommunicationType.ERROR
            }
            return
        }

        if (lastLocation!!.world == null) {
            player.sendTransText("store.player.teleport.unloaded") {
                type = CommunicationType.ERROR
            }
            return
        }

        player.sendTransText("store.player.teleport.success") {
            type = CommunicationType.SUCCESS
        }
        player.teleportAsync(lastLocation!!)
    }

    fun toggleBuildMode() {
        val messageKey = if (isBuilding) "store.player.build.remove" else "store.player.build.add"
        player.sendTransText(messageKey) {
            type = CommunicationType.UPDATE
        }
        isBuilding = !isBuilding
    }

    fun toggleDarkMode() {
        if (isDarkMode) {
            player.sendTransText("store.player.dark.remove") {
                type = CommunicationType.UPDATE
            }

            player.removePotionEffect(PotionEffectType.NIGHT_VISION)
            isDarkMode = false
            return
        }

        player.addPotionEffect(
            PotionEffect(
                PotionEffectType.NIGHT_VISION,
                PotionEffect.INFINITE_DURATION,
                1,
                false,
                false,
                false
            )
        )
        player.sendTransText("store.player.dark.add") {
            type = CommunicationType.UPDATE
        }

        isDarkMode = true
    }

    fun cancelWhenBuilder(event: Cancellable) {
        event.isCancelled = !this.isBuilding
    }

    fun hasActiveNightVision(): Boolean {
        for (potions in player.activePotionEffects) {

            if (potions.type != PotionEffectType.NIGHT_VISION) continue
            return true
        }

        return false
    }

}