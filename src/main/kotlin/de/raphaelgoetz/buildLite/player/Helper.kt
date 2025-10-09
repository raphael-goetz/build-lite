package de.raphaelgoetz.buildLite.player

import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.PREFIX
import de.raphaelgoetz.buildLite.cache.CachePlayerProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.`object`.ObjectContents
import net.kyori.adventure.text.`object`.PlayerHeadObjectContents
import org.bukkit.entity.Player

fun Player.hasWorldEnterPermission(name: String, group: String): Boolean {
    if (hasPermission("build-lite.*")) return true
    if (hasPermission("build-lite.world.enter.*")) return true
    if (hasPermission("build-lite.world.enter.$group.*")) return true
    if (hasPermission("build-lite.world.enter.$group.$name")) return true

    sendMessage(adventureText("$PREFIX No permission access this world.") {
        color = Colorization.RED
    })
    return false
}

fun createPlayerHead(profile: CachePlayerProfile, hat: Boolean = true): Component {
    val headBuilder = ObjectContents.playerHead().apply {
        id(profile.playerUUID)
        name(profile.playerName)
        hat(hat)

        // Add textures if available
        profile.playerProfile.properties.forEach { prop ->
            profileProperty(
                PlayerHeadObjectContents.property(prop.name, prop.value, prop.signature)
            )
        }
    }.build()

    return Component.`object`().contents(headBuilder).build()
}

fun Player.checkPermission(permission: String): Boolean {
    if (!hasPermission(permission) && !hasPermission("build-lite.*")) {
        sendMessage(adventureText("$PREFIX No permission to perform this action.") {
            color = Colorization.RED
        })
        return false
    }
    return true
}