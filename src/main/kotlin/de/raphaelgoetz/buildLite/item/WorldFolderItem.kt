package de.raphaelgoetz.buildLite.item

import de.raphaelgoetz.astralis.items.builder.SmartLoreBuilder
import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.components.RenderMode
import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.formatting.capitalizeFirst
import de.raphaelgoetz.buildLite.menu.openWorldDisplayMenu
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.world.WorldFolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import java.net.URI
import java.util.UUID

fun Player.createWorldFolderItem(worldFolder: WorldFolder): SmartClick {
    val item = createSmartItem<SkullMeta>(
        name = worldFolder.group.capitalizeFirst(),
        material = Material.PLAYER_HEAD,
        interactionType = InteractionType.DISPLAY_CLICK,
        tagResolver = listOf(Placeholder.parsed("folder", name))
    ) {
        val newPlayerProfile = Bukkit.createProfile(UUID.randomUUID())
        val playerTextures = newPlayerProfile.textures
        val categoryTextureURL = URI.create(DisplayURL.ITEM_CATEGORY.url).toURL()

        playerTextures.skin = categoryTextureURL
        newPlayerProfile.setTextures(playerTextures)
        playerProfile = newPlayerProfile

        val description = getFolderDescription(worldFolder.worlds)
        this.lore(description)
    }

    return SmartClick(item) {
        openWorldDisplayMenu(worldFolder)
    }
}

private fun getFolderDescription(worlds: List<RecordWorld>): List<Component> {
    val maxEntries = 20
    val hasMore = worlds.size > maxEntries
    val displayWorlds = worlds.take(maxEntries)

    val lines = displayWorlds.mapIndexed { index, item ->
        val prefix = if (index == displayWorlds.lastIndex && !hasMore) "└ " else "├ "
        adventureText("$prefix${item.name.capitalizeFirst()}") {
            type = CommunicationType.NONE
            color = Colorization.BLUE
            renderMode = RenderMode.LORE
        }
    }.toMutableList()

    if (hasMore) {
        lines.add(
            adventureText("└ and more...") {
                type = CommunicationType.NONE
            }
        )
    }

    lines.add(" ".gray())
    lines.add("Left-Click > Open World Folder".gray())

    // Prepend the base description and build
    return SmartLoreBuilder(lines).build()
}
