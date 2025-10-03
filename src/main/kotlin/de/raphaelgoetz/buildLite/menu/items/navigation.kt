package de.raphaelgoetz.buildLite.menu.items

import de.raphaelgoetz.astralis.items.basicSmartTransItem
import de.raphaelgoetz.astralis.items.builder.SmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.items.smartTransItem
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.buildLite.menu.openBannerCreationMenu
import de.raphaelgoetz.buildLite.menu.openPlayerOverviewMenu
import de.raphaelgoetz.buildLite.menu.openWarpMenu
import de.raphaelgoetz.buildLite.menu.openWorldCategoryMenu
import de.raphaelgoetz.buildLite.record.getGlobalWarps
import de.raphaelgoetz.buildLite.record.getPrivateWarps
import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta

/*
 * Arrow Items
 */
fun BuildPlayer.createLeftPageItem(): SmartItem {
    return player.smartTransItem<ItemMeta>(
        "gui.item.arrow.left",
        material = Material.ARROW,
        interactionType = InteractionType.PAGE_TURN
    )
}

fun BuildPlayer.createRightPageItem(): SmartItem {
    return player.smartTransItem<ItemMeta>(
        "gui.item.arrow.right",
        material = Material.ARROW,
        interactionType = InteractionType.PAGE_TURN
    )
}

/*
 * Home Menu Items
 */
fun BuildPlayer.createHomeWorldClick(buildServer: BuildServer): SmartClick {
    val item = player.basicSmartTransItem(
        key = "gui.main.item.world.name",
        descriptionKey = "gui.main.item.world.lore",
        material = Material.GRASS_BLOCK,
        interactionType = InteractionType.DISPLAY_CLICK
    )

    return SmartClick(item) { event ->
        event.isCancelled = true
        player.closeInventory()
        openWorldCategoryMenu(buildServer)
    }
}

fun BuildPlayer.createHomePlayerClick(buildServer: BuildServer): SmartClick {
    val item = player.smartTransItem<SkullMeta>(
        key = "gui.player.item.world.name",
        descriptionKey = "gui.player.item.world.lore",
        material = Material.PLAYER_HEAD,
        interactionType = InteractionType.DISPLAY_CLICK
    ) {
        owningPlayer = player
    }

    return SmartClick(item) { event ->
        event.isCancelled = true
        player.closeInventory()

        openPlayerOverviewMenu(buildServer)
    }
}

fun BuildPlayer.createHomeBannerClick(buildServer: BuildServer): SmartClick {
    val item = player.smartTransItem<BannerMeta>(
        key = "gui.main.item.banner.name",
        descriptionKey = "gui.main.item.banner.lore",
        material = Material.GREEN_BANNER,
        interactionType = InteractionType.DISPLAY_CLICK
    ) {
        addPattern(Pattern(DyeColor.BLACK, PatternType.GLOBE))
    }

    return SmartClick(item) { event ->
        event.isCancelled = true
        player.closeInventory()

        openBannerCreationMenu(buildServer)
    }
}

fun BuildPlayer.createHomePrivateWarpClick(buildServer: BuildServer): SmartClick {
    val item = player.basicSmartTransItem(
        key = "gui.main.item.privatewarps.name",
        descriptionKey = "gui.main.item.privatewarps.lore",
        material = Material.ENDER_EYE,
        interactionType = InteractionType.DISPLAY_CLICK
    )

    return SmartClick(item) { event ->
        event.isCancelled = true
        player.closeInventory()

        val warps = getPrivateWarps()
        openWarpMenu(warps, buildServer)
    }
}

fun BuildPlayer.createHomePublicWarpClick(buildServer: BuildServer): SmartClick {
    val item = player.basicSmartTransItem(
        key = "gui.main.item.publicwarps.name",
        descriptionKey = "gui.main.item.publicwarps.lore",
        material = Material.ENDER_PEARL,
        interactionType = InteractionType.DISPLAY_CLICK
    )

    return SmartClick(item) { event ->
        event.isCancelled = true
        player.closeInventory()

        val warps = getGlobalWarps()
        openWarpMenu(warps, buildServer)
    }
}