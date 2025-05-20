package de.raphaelgoetz.buildLite.registry

import de.raphaelgoetz.astralis.items.basicItemWithoutMeta
import de.raphaelgoetz.astralis.items.builder.SmartItem
import de.raphaelgoetz.astralis.items.smartItemWithoutMeta
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class BannerColors(val dyeColor: DyeColor, val dyeItem: SmartItem, val dyedBannerItem: ItemStack) {
    WHITE(
        DyeColor.WHITE,
        smartItemWithoutMeta("", Material.WHITE_DYE),
        basicItemWithoutMeta(Material.WHITE_BANNER)
    ),
    ORANGE(
        DyeColor.ORANGE,
        smartItemWithoutMeta("", Material.ORANGE_DYE),
        basicItemWithoutMeta(Material.ORANGE_BANNER)
    ),
    MAGENTA(
        DyeColor.MAGENTA,
        smartItemWithoutMeta("", Material.MAGENTA_DYE),
        basicItemWithoutMeta(Material.MAGENTA_BANNER)
    ),
    LIGHT_BLUE(
        DyeColor.LIGHT_BLUE,
        smartItemWithoutMeta("", Material.LIGHT_BLUE_DYE),
        basicItemWithoutMeta(Material.LIGHT_BLUE_BANNER)
    ),
    YELLOW(
        DyeColor.YELLOW,
        smartItemWithoutMeta("", Material.YELLOW_DYE),
        basicItemWithoutMeta(Material.YELLOW_BANNER)
    ),
    LIME(
        DyeColor.LIME,
        smartItemWithoutMeta("", Material.LIME_DYE),
        basicItemWithoutMeta(Material.LIME_BANNER)),
    PINK(
        DyeColor.PINK,
        smartItemWithoutMeta("", Material.PINK_DYE),
        basicItemWithoutMeta(Material.PINK_BANNER)
    ),
    GRAY(
        DyeColor.GRAY,
        smartItemWithoutMeta("", Material.GRAY_DYE),
        basicItemWithoutMeta(Material.GRAY_BANNER)
    ),
    LIGHT_GRAY(
        DyeColor.LIGHT_GRAY,
        smartItemWithoutMeta("", Material.LIGHT_GRAY_DYE),
        basicItemWithoutMeta(Material.LIGHT_GRAY_BANNER)
    ),
    CYAN(
        DyeColor.CYAN,
        smartItemWithoutMeta("", Material.CYAN_DYE),
        basicItemWithoutMeta(Material.CYAN_BANNER)
    ),
    PURPLE(
        DyeColor.PURPLE,
        smartItemWithoutMeta("", Material.PURPLE_DYE),
        basicItemWithoutMeta(Material.PURPLE_BANNER)
    ),
    BLUE(DyeColor.BLUE,
        smartItemWithoutMeta("", Material.BLUE_DYE),
        basicItemWithoutMeta(Material.BLUE_BANNER)),
    BROWN(
        DyeColor.BROWN,
        smartItemWithoutMeta("", Material.BROWN_DYE),
        basicItemWithoutMeta(Material.BROWN_BANNER)
    ),
    GREEN(
        DyeColor.GREEN,
        smartItemWithoutMeta("", Material.GREEN_DYE),
        basicItemWithoutMeta(Material.GREEN_BANNER)
    ),
    RED(
        DyeColor.RED,
        smartItemWithoutMeta("", Material.RED_DYE),
        basicItemWithoutMeta(Material.RED_BANNER)),
    BLACK(
        DyeColor.BLACK,
        smartItemWithoutMeta("", Material.BLACK_DYE),
        basicItemWithoutMeta(Material.BLACK_BANNER)
    )
}

val bannerColors: List<BannerColors>
    get() = BannerColors.entries.toList()