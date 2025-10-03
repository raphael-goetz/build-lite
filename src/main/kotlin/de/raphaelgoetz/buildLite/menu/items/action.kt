package de.raphaelgoetz.buildLite.menu.items

import de.raphaelgoetz.astralis.items.basicSmartTransItem
import de.raphaelgoetz.astralis.items.builder.SmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import org.bukkit.Material

/*
 * Home
 */
fun BuildPlayer.createHomePhysicItem(buildServer: BuildServer): SmartItem {
    val world = buildServer.asBuildWorld(player.world)
    val active = world!!.hasPhysics
    val name = if (active) "gui.main.item.physics.enable.name" else "gui.main.item.physics.disable.name"
    val interactionType = if (active) InteractionType.ENABLED else InteractionType.DISABLED

    return player.basicSmartTransItem(
        key = name,
        descriptionKey = "gui.main.item.physics.lore",
        material = Material.GRAVEL,
        interactionType = interactionType,
    )
}

fun BuildPlayer.createHomeFlySpeedClick(): SmartClick {
    val item = player.basicSmartTransItem(
        key = "gui.main.item.flyspeed.name",
        descriptionKey = "gui.main.item.flyspeed.lore",
        material = Material.FEATHER,
        interactionType = InteractionType.CLICK
    )

    return SmartClick(item) {

    }
}