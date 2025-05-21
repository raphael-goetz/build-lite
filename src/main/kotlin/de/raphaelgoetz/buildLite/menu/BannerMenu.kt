package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.items.builder.SmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.items.smartTransItem
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.registry.bannerColors
import de.raphaelgoetz.buildLite.registry.getItemWithURL
import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.ItemMeta

private val baseBannerItems = bannerColors.map { it.dyedBannerItem }
private val basePatterns = RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN)

private fun getColoredPatterns(color: DyeColor): List<Pattern> = basePatterns.map { Pattern(color, it) }

private fun ItemStack.checkMaxPatterns(player: BuildPlayer) {
    val player = player.player
    if (itemMeta is BannerMeta) {

        val bannerMeta = itemMeta as BannerMeta
        if (bannerMeta.patterns.size != 6) return
        player.closeInventory()
        player.inventory.addItem(this)
    }
}

private fun ItemStack.applyPatterns(itemStack: ItemStack): ItemStack {
    if (itemMeta is BannerMeta && itemStack.itemMeta is BannerMeta) {
        val currBannerMeta = itemMeta as BannerMeta
        val nextBannerMeta = itemStack.itemMeta as BannerMeta

        val patterns = nextBannerMeta.patterns
        currBannerMeta.patterns = patterns
        itemMeta = currBannerMeta
    }

    return this
}

private fun ItemStack.addPattern(pattern: Pattern): ItemStack {
    if (itemMeta is BannerMeta) {

        val bannerMeta = itemMeta as BannerMeta
        bannerMeta.addPattern(pattern)
        itemMeta = bannerMeta
    }

    return this
}

private fun ItemStack.removeLatestPattern(): ItemStack {
    if (itemMeta is BannerMeta) {

        val bannerMeta = itemMeta as BannerMeta
        val size = bannerMeta.patterns.size
        if (size == 0) return this

        bannerMeta.removePattern(size - 1)
        itemMeta = bannerMeta
    }

    return this
}


private fun BuildPlayer.openColorSelector(banner: ItemStack, server: BuildServer) {

    val baseColors = bannerColors.map { bannerColor ->
        SmartClick(bannerColor.dyeItem) { event ->
            event.isCancelled = true
            openPatternSelector(bannerColor.dyeColor, banner, server)
            banner.checkMaxPatterns(this@openColorSelector)
        }
    }

    player.openTransPageInventory("gui.banner.color.title", "Select the next pattern color", InventoryRows.ROW6, baseColors) {

        val close = player.getItemWithURL(
            Material.BARRIER, DisplayURL.GUI_CLOSE.url, player.locale().getValue("gui.item.main.menu")
        )

        setBlockedSlot(InventorySlots.SLOT5ROW6, close) { event ->
            event.isCancelled = true
            openMainMenu(server)
        }

        val bannerItem = player.smartTransItem<BannerMeta>("gui.banner.item.get", material = banner.type, interactionType = InteractionType.SUCCESS)
        val newBannerItem = bannerItem.itemStack.applyPatterns(banner)

        setBlockedSlot(InventorySlots.SLOT6ROW6, SmartItem(newBannerItem, InteractionType.SUCCESS)) { event ->
            event.isCancelled = true
            player.closeInventory()
            player.inventory.addItem(banner)
        }

        val removeItem = player.smartTransItem<ItemMeta>("gui.banner.item.remove", material = banner.type, interactionType = InteractionType.DISABLED)
        setBlockedSlot(InventorySlots.SLOT4ROW6, removeItem) { event ->
            event.isCancelled = true
            val item = banner.removeLatestPattern()
            openColorSelector(item, server)
        }
    }
}

private fun BuildPlayer.openPatternSelector(dyeColor: DyeColor, banner: ItemStack, server: BuildServer) {

    val patterns = getColoredPatterns(dyeColor).map { pattern ->
        val item = ItemStack(banner)
        SmartClick(SmartItem(item.addPattern(pattern), InteractionType.CLICK)) { event ->
            event.isCancelled = true
            openColorSelector(banner.addPattern(pattern), server)
            banner.checkMaxPatterns(this@openPatternSelector)
        }
    }

    player.openTransPageInventory("gui.banner.pattern.title", "Select the next pattern", InventoryRows.ROW6, patterns) {
        val left = player.smartTransItem<ItemMeta>("gui.item.arrow.left" , material = Material.ARROW)
        val right = player.smartTransItem<ItemMeta>("gui.item.arrow.right" , material = Material.ARROW)
        pageLeft(InventorySlots.SLOT1ROW6, left.itemStack)
        pageRight(InventorySlots.SLOT9ROW6, right.itemStack)

        val close = player.getItemWithURL(
            Material.BARRIER, DisplayURL.GUI_CLOSE.url, player.locale().getValue("gui.item.main.menu")
        )

        setBlockedSlot(InventorySlots.SLOT5ROW6, close) { event ->
            event.isCancelled = true
            openMainMenu(server)
        }

        val bannerItem = player.smartTransItem<BannerMeta>("gui.banner.item.get", material = Material.BARRIER, interactionType = InteractionType.SUCCESS)
        val newBannerItem = bannerItem.itemStack.applyPatterns(banner)

        setBlockedSlot(InventorySlots.SLOT6ROW6, SmartItem(newBannerItem, InteractionType.SUCCESS)) { event ->
            event.isCancelled = true
            player.closeInventory()
            player.inventory.addItem(banner)
        }

        val removeItem = player.smartTransItem<ItemMeta>("gui.banner.item.remove", material = Material.BARRIER, interactionType = InteractionType.DISABLED)
        setBlockedSlot(InventorySlots.SLOT4ROW6, removeItem) { event ->
            event.isCancelled = true
            val item = banner.removeLatestPattern()
            openColorSelector(item, server)
        }
    }
}


fun BuildPlayer.openBannerCreationMenu(server: BuildServer) {
    val baseBanners = baseBannerItems.map { bannerItem ->
        val item = ItemStack(bannerItem)
        SmartClick(SmartItem(item, InteractionType.CLICK)) { event ->
            event.isCancelled = true
            openColorSelector(item, server)
        }
    }

    player.openTransPageInventory("gui.banner.base.title", "Select a base Banner", InventoryRows.ROW6, baseBanners) {

        val close = player.getItemWithURL(
            Material.BARRIER, DisplayURL.GUI_CLOSE.url, player.locale().getValue("gui.item.main.menu")
        )

        setBlockedSlot(InventorySlots.SLOT5ROW6, close) { event ->
            event.isCancelled = true
            openMainMenu(server)
        }
    }
}