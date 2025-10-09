package de.raphaelgoetz.buildLite.action

import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.PREFIX
import de.raphaelgoetz.buildLite.cache.CacheReview
import de.raphaelgoetz.buildLite.cache.PlayerCache
import de.raphaelgoetz.buildLite.sql.updateSqlPlayer
import de.raphaelgoetz.buildLite.world.toLoadableLocation
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

fun Player.actionUpdateLastLocation(location: Location) {
    val loadableLocation = location.toLoadableLocation()

    loadableLocation?.let {
        updateSqlPlayer(location = it)
    }

    PlayerCache.refresh(this)
}

fun Player.actionEnableBuildMode() {
    updateSqlPlayer(buildMode = true)
    PlayerCache.refresh(this)
}

fun Player.actionEnableNightMode() {
    updateSqlPlayer(nightMode = true)

    addPotionEffect(
        PotionEffect(
            PotionEffectType.NIGHT_VISION,
            PotionEffect.INFINITE_DURATION,
            1,
            false,
            false,
            false
        )
    )

    PlayerCache.refresh(this)
}

fun Player.actionDisableBuildMode() {
    updateSqlPlayer(buildMode = false)
    PlayerCache.refresh(this)
}

fun Player.actionDisableNightMode() {
    updateSqlPlayer(nightMode = false)
    removePotionEffect(PotionEffectType.NIGHT_VISION)
    PlayerCache.refresh(this)
}

fun Player.actionEnableReviewMode() {
    updateSqlPlayer(reviewMode = true)
    CacheReview.showAll(this)
    PlayerCache.refresh(this)
}

fun Player.actionDisableReviewMode() {
    updateSqlPlayer(reviewMode = false)
    CacheReview.hideAll(this)
    PlayerCache.refresh(this)
}