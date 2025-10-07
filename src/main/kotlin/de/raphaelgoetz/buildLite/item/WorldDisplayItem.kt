package de.raphaelgoetz.buildLite.item

import de.raphaelgoetz.astralis.items.builder.SmartLoreBuilder
import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.text.components.RenderMode
import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.action.actionWorldFavoriteToggle
import de.raphaelgoetz.buildLite.cache.CachePlayerProfile
import de.raphaelgoetz.buildLite.cache.PlayerProfileCache
import de.raphaelgoetz.buildLite.dialog.world.showWorldEditPropertyDialog
import de.raphaelgoetz.buildLite.formatting.capitalizeFirst
import de.raphaelgoetz.buildLite.menu.openWarpMenu
import de.raphaelgoetz.buildLite.menu.openWorldFolderMenu
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.sql.RecordPlayerCredit
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.getSqlPlayerCredits
import de.raphaelgoetz.buildLite.sql.hasSqlPlayerFavorite
import de.raphaelgoetz.buildLite.world.LoadableWorld
import de.raphaelgoetz.buildLite.world.WorldLoader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.`object`.ObjectContents
import net.kyori.adventure.text.`object`.PlayerHeadObjectContents
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import java.net.URI
import java.util.*

fun Player.createWorldDisplayItem(recordWorld: RecordWorld): SmartClick {
    val loadableWorld = LoadableWorld(recordWorld.uniqueId)
    val credits = loadableWorld.getSqlPlayerCredits()

    val isFavorite = hasSqlPlayerFavorite(recordWorld.uniqueId)
    val name = if (isFavorite) "★ " + recordWorld.name.capitalizeFirst() + " ★" else recordWorld.name.capitalizeFirst()
    val item = createSmartItem<SkullMeta>(
        name = name,
        material = Material.PLAYER_HEAD,
        interactionType = InteractionType.DISPLAY_CLICK,
    ) {
        val newPlayerProfile = Bukkit.createProfile(UUID.randomUUID())
        val playerTextures = newPlayerProfile.textures
        val categoryTextureURL = URI.create(DisplayURL.ITEM_WORLD.url).toURL()

        playerTextures.skin = categoryTextureURL
        newPlayerProfile.setTextures(playerTextures)
        playerProfile = newPlayerProfile

        val description = getDescription(recordWorld, credits, isFavorite)
        this.lore(description)
    }

    return SmartClick(item) { click ->
        click.isCancelled = true

        if (click.isShiftClick && click.isLeftClick) {
            openWarpMenu(recordWorld.uniqueId)
            return@SmartClick
        }

        if (click.isShiftClick && click.isRightClick) {
            actionWorldFavoriteToggle(recordWorld.uniqueId)
            openWorldFolderMenu()
            return@SmartClick
        }

        if (click.isLeftClick) {
            closeInventory()
            WorldLoader.lazyTeleport(recordWorld.loadableSpawn, recordWorld.generator, this)
            return@SmartClick
        }

        if (click.isRightClick) {
            showWorldEditPropertyDialog(recordWorld)
            return@SmartClick
        }
    }
}

private fun getDescription(
    recordWorld: RecordWorld, credits: List<RecordPlayerCredit>, isFavorite: Boolean
): List<Component> {
    val profile = PlayerProfileCache.getOrFetch(recordWorld.creatorUuid)
    val firstSection =
        mutableListOf("Status: ${recordWorld.state.text}".gray(), Component.text(" "), "Created by: ".gray().append {
            createPlayerHead(profile).append {
                adventureText(" ${Bukkit.getOfflinePlayer(recordWorld.creatorUuid).name}") {
                    bold(true)
                    color = Colorization.LIME
                }
            }

        }, "Generator: ${recordWorld.generator.text}".gray(), "Physics: ".gray().append {
            val text = if (recordWorld.physicsEnabled) "Enabled" else "Disabled"
            adventureText(text) {
                bold(true)
                val col = if (recordWorld.physicsEnabled) Colorization.LIME else Colorization.RED
                color = col
            }
        })

    val middleSection = mutableListOf<Component>()

    if (credits.isNotEmpty()) {
        middleSection.add(Component.text(" "))
        middleSection.add("Credits:".gray())
        credits.forEachIndexed { index, item ->
            val profile = PlayerProfileCache.getOrFetch(item.playerUuid)
            val name = " ${profile.playerName}"
            val prefix = if (index == credits.lastIndex) "└ " else "├ "
            middleSection.add(
                prefix.gray().append {
                    createPlayerHead(profile).append {
                        name.gray()
                    }
                })
        }

        middleSection.add(Component.text(" "))
    } else {
        middleSection.add(Component.text(" "))
    }

    val lastSection = mutableListOf(
        "Left-Click > Join World".gray(),
        "Right-Click > Manage/Delete Properties".gray(),
        "Shift + Left-Click > Open World Warps".gray(),
        if (isFavorite) "Shift + Right-Click > Unpin World".gray() else "Shift + Left-Click > Pin World".gray(),
    )

    val result = firstSection + middleSection + lastSection
    return SmartLoreBuilder(result.toMutableList()).build()
}

fun String.gray(): Component = adventureText(this) {
    color = Colorization.LIGHT_GRAY
    renderMode = RenderMode.LORE
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