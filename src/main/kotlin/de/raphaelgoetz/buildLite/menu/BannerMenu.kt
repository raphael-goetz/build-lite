package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.items.builder.SmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.items.smartTransItem
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import de.raphaelgoetz.buildLite.item.createInactivePageLeftItem
import de.raphaelgoetz.buildLite.item.createInactivePageRightItem
import de.raphaelgoetz.buildLite.item.createPageLeftItem
import de.raphaelgoetz.buildLite.item.createPageRightItem
import de.raphaelgoetz.buildLite.registry.BannerColors
import de.raphaelgoetz.buildLite.registry.DisplayURL
import de.raphaelgoetz.buildLite.registry.getItemWithURL
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BannerMeta

fun Player.openBannerCreationMenu() {
    val menu = BannerCreationMenu()
    menu.openBannerCreationMenu(this)
}

private class BannerCreationMenu() {

    // All dye colors available as base banners
    private val bannerColors = BannerColors.entries

    private val baseBannerItems = bannerColors.map { color ->
        ItemStack(color.dyedBannerItem).apply {
            val meta = itemMeta as BannerMeta
            itemMeta = meta
        }
    }

    private fun getColoredPatterns(color: DyeColor): List<Pattern> {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN).map { Pattern(color, it) }
    }

    private fun ItemStack.checkMaxPatterns(player: Player) {
        val meta = itemMeta as? BannerMeta ?: return
        if (meta.patterns.size >= 6) {
            player.sendMessage(Component.text("✅ Your banner has reached the maximum of 6 patterns."))
            player.closeInventory()
            player.inventory.addItem(this)
        }
    }

    private fun ItemStack.applyPatterns(from: ItemStack): ItemStack {
        val newItem = this.clone()
        val fromMeta = from.itemMeta as? BannerMeta ?: return newItem
        val newMeta = newItem.itemMeta as? BannerMeta ?: return newItem

        newMeta.patterns = fromMeta.patterns
        newItem.itemMeta = newMeta
        return newItem
    }

    private fun ItemStack.addPattern(pattern: Pattern): ItemStack {
        val newItem = this.clone()
        val meta = newItem.itemMeta as? BannerMeta ?: return newItem
        meta.addPattern(pattern)
        newItem.itemMeta = meta
        return newItem
    }

    private fun ItemStack.removeLatestPattern(): ItemStack {
        val newItem = this.clone()
        val meta = newItem.itemMeta as? BannerMeta ?: return newItem
        if (meta.patterns.isNotEmpty()) {
            meta.removePattern(meta.patterns.size - 1)
            newItem.itemMeta = meta
        }
        return newItem
    }

    fun openBannerCreationMenu(player: Player) {
        val baseBanners = baseBannerItems.map { bannerItem ->
            val item = ItemStack(bannerItem)
            SmartClick(SmartItem(item, InteractionType.CLICK)) { event ->
                event.isCancelled = true
                player.openColorSelector(item)
            }
        }

        player.openTransPageInventory("gui.banner.base.title", "Select a base Banner", InventoryRows.ROW6, baseBanners) {
            val close = player.getItemWithURL(
                Material.BARRIER,
                DisplayURL.GUI_CLOSE.url,
                player.locale().getValue("gui.item.main.menu")
            )

            setBlockedSlot(InventorySlots.SLOT5ROW6, close) { event ->
                event.isCancelled = true
                player.showHomeDialog()
            }
        }
    }

    private fun Player.openColorSelector(banner: ItemStack) {
        val baseColors = bannerColors.map { bannerColor ->
            val item = ItemStack(Material.valueOf("${bannerColor.name}_DYE"))
            SmartClick(SmartItem(item, InteractionType.CLICK)) { event ->
                event.isCancelled = true
                openPatternSelector(bannerColor.dyeColor, banner)
            }
        }

        openTransPageInventory(
            "gui.banner.color.title",
            "Select the next pattern color",
            InventoryRows.ROW6,
            baseColors
        ) {
            val close = getItemWithURL(
                Material.BARRIER,
                DisplayURL.GUI_CLOSE.url,
                locale().getValue("gui.item.main.menu")
            )

            setBlockedSlot(InventorySlots.SLOT5ROW6, close) { event ->
                event.isCancelled = true
                closeInventory()
            }

            val bannerItem = smartTransItem<BannerMeta>(
                "gui.banner.item.get",
                material = banner.type,
                interactionType = InteractionType.SUCCESS
            )
            val newBannerItem = bannerItem.itemStack.applyPatterns(banner)

            setBlockedSlot(InventorySlots.SLOT6ROW6, SmartItem(newBannerItem, InteractionType.SUCCESS)) { event ->
                event.isCancelled = true
                closeInventory()
                inventory.addItem(banner)
            }

            val removeItem = smartTransItem<BannerMeta>(
                "gui.banner.item.remove",
                material = banner.type,
                interactionType = InteractionType.DISABLED
            )
            setBlockedSlot(InventorySlots.SLOT4ROW6, removeItem) { event ->
                event.isCancelled = true
                val item = banner.removeLatestPattern()
                openColorSelector(item)
            }
        }
    }

    private fun Player.openPatternSelector(dyeColor: DyeColor, banner: ItemStack) {
        val patterns = getColoredPatterns(dyeColor).map { pattern ->
            val item = banner.clone().addPattern(pattern)
            SmartClick(SmartItem(item, InteractionType.CLICK)) { event ->
                event.isCancelled = true
                openColorSelector(banner.addPattern(pattern))
                banner.checkMaxPatterns(this@openPatternSelector)
            }
        }

        openTransPageInventory("gui.banner.pattern.title", "Select the next pattern", InventoryRows.ROW6, patterns) {
            pageLeft(InventorySlots.SLOT1ROW6, createPageLeftItem(), createInactivePageLeftItem())
            pageRight(InventorySlots.SLOT9ROW6, createPageRightItem(), createInactivePageRightItem())

            val close = getItemWithURL(
                Material.BARRIER,
                DisplayURL.GUI_CLOSE.url,
                locale().getValue("gui.item.main.menu")
            )

            setBlockedSlot(InventorySlots.SLOT5ROW6, close) { event ->
                event.isCancelled = true
                closeInventory()
            }

            val bannerItem = smartTransItem<BannerMeta>(
                "gui.banner.item.get",
                material = banner.type,
                interactionType = InteractionType.SUCCESS
            )
            val newBannerItem = bannerItem.itemStack.applyPatterns(banner)

            setBlockedSlot(InventorySlots.SLOT6ROW6, SmartItem(newBannerItem, InteractionType.SUCCESS)) { event ->
                event.isCancelled = true
                closeInventory()
                inventory.addItem(banner)
            }

            val removeItem = smartTransItem<BannerMeta>(
                "gui.banner.item.remove",
                material = banner.type,
                interactionType = InteractionType.DISABLED
            )
            setBlockedSlot(InventorySlots.SLOT4ROW6, removeItem) { event ->
                event.isCancelled = true
                val item = banner.removeLatestPattern()
                openColorSelector(item)
            }
        }
    }
}