package de.raphaelgoetz.buildLite.item

import de.raphaelgoetz.astralis.items.smartTransItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun Player.createPageLeftItem(): ItemStack {
    return smartTransItem<ItemMeta>("gui.item.arrow.left", material = Material.ARROW).itemStack
}