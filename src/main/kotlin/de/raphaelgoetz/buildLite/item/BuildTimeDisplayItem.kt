package de.raphaelgoetz.buildLite.item

import de.raphaelgoetz.astralis.items.builder.SmartLoreBuilder
import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.buildLite.dialog.buildtime.showBuildTimeIntervalDialog
import de.raphaelgoetz.buildLite.cache.CachePlayerProfile
import de.raphaelgoetz.buildLite.formatting.capitalizeFirst
import de.raphaelgoetz.buildLite.formatting.formatDuration
import de.raphaelgoetz.buildLite.player.hasWorldEnterPermission
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.world.WorldLoader
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import java.net.URI
import java.util.UUID

fun Player.createWorldBuildTimeDisplayItem(recordWorld: RecordWorld, seconds: Long): SmartClick {
    val item = createSmartItem<SkullMeta>(
        name = recordWorld.name.capitalizeFirst(),
        material = Material.PLAYER_HEAD,
        interactionType = InteractionType.DISPLAY_CLICK,
    ) {
        val newPlayerProfile = Bukkit.createProfile(UUID.randomUUID())
        val playerTextures = newPlayerProfile.textures
        playerTextures.skin = URI.create(DisplayURL.ITEM_WORLD.url).toURL()
        newPlayerProfile.setTextures(playerTextures)
        playerProfile = newPlayerProfile

        lore(
            SmartLoreBuilder(
                mutableListOf(
                    "Time: ${seconds.formatDuration()}".gray(),
                    "".gray(),
                    "Left-Click > Join World".gray(),
                )
            ).build()
        )
    }

    return SmartClick(item) { click ->
        click.isCancelled = true
        if (!click.isLeftClick) return@SmartClick
        if (!hasWorldEnterPermission(recordWorld.name, recordWorld.group)) return@SmartClick

        closeInventory()
        WorldLoader.lazyTeleport(recordWorld.loadableSpawn, recordWorld.generator, this)
    }
}

fun Player.createBuildTimePlayerPickerItem(target: CachePlayerProfile): SmartClick {
    val item = createSmartItem<SkullMeta>(
        name = target.playerName,
        material = Material.PLAYER_HEAD,
        interactionType = InteractionType.DISPLAY_CLICK,
    ) {
        playerProfile = target.playerProfile
        lore(
            SmartLoreBuilder(
                mutableListOf(
                    "".gray(),
                    "Left-Click > View Build Time".gray(),
                )
            ).build()
        )
    }

    return SmartClick(item) { click ->
        click.isCancelled = true
        if (!click.isLeftClick) return@SmartClick
        showBuildTimeIntervalDialog(target.playerUUID, target.playerName)
    }
}
