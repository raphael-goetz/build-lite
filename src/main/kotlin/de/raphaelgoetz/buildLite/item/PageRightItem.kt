package de.raphaelgoetz.buildLite.item

import de.raphaelgoetz.astralis.items.smartTransItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun Player.createPageRightItem(): ItemStack {
    return smartTransItem<ItemMeta>("gui.item.arrow.right", material = Material.ARROW).itemStack
}