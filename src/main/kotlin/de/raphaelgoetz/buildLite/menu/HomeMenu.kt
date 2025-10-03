package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.items.basicSmartTransItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.items.smartTransItem
import de.raphaelgoetz.astralis.ui.builder.InventoryBuilder
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransInventory
import de.raphaelgoetz.buildLite.menu.items.createHomeBannerClick
import de.raphaelgoetz.buildLite.menu.items.createHomePlayerClick
import de.raphaelgoetz.buildLite.menu.items.createHomePrivateWarpClick
import de.raphaelgoetz.buildLite.menu.items.createHomePublicWarpClick
import de.raphaelgoetz.buildLite.menu.items.createHomeWorldClick
import de.raphaelgoetz.buildLite.record.getGlobalWarps
import de.raphaelgoetz.buildLite.record.getPrivateWarps
import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.SkullMeta

fun InventoryBuilder.setSmartClick(slot: InventorySlots, click: SmartClick) {
    setSlot(slot, click.item, click.action)
}

fun BuildPlayer.openMainMenu(buildServer: BuildServer) {
    player.openTransInventory("gui.main.title", "Main-Menu", InventoryRows.ROW2) {

        this.setSmartClick(InventorySlots.SLOT2ROW2, createHomePublicWarpClick(buildServer))
        this.setSmartClick(InventorySlots.SLOT3ROW2, createHomePrivateWarpClick(buildServer))
        this.setSmartClick(InventorySlots.SLOT2ROW1, createHomeWorldClick(buildServer))
        this.setSmartClick(InventorySlots.SLOT3ROW1, createHomePlayerClick(buildServer))
        this.setSmartClick(InventorySlots.SLOT4ROW1, createHomeBannerClick(buildServer))

        fun setPhysicItems() {

            val world = buildServer.asBuildWorld(player.world) ?: return
            val active = world.hasPhysics
            val name =
                if (active) "gui.main.item.physics.enable.name" else "gui.main.item.physics.disable.name"
            val interactionType = if (active) InteractionType.ENABLED else InteractionType.DISABLED

            val item = player.basicSmartTransItem(
                key = name,
                descriptionKey = "gui.main.item.physics.lore",
                material = Material.GRAVEL,
                interactionType = interactionType,
            )

            this.setBlockedSlot(InventorySlots.SLOT6ROW1, item) {
                world.togglePhysics()
                setPhysicItems()
            }
        }

        fun setBuildItems() {
            val active = this@openMainMenu.isBuilding
            val name =
                if (active) "gui.main.item.build.enable.name" else "gui.main.item.build.disable.name"
            val interactionType = if (active) InteractionType.ENABLED else InteractionType.DISABLED

            val item = player.basicSmartTransItem(
                key = name,
                descriptionKey = "gui.main.item.build.lore",
                material = Material.DIAMOND_AXE,
                interactionType = interactionType,
            )

            this.setBlockedSlot(InventorySlots.SLOT7ROW1, item) {
                this@openMainMenu.toggleBuildMode()
                setBuildItems()
            }
        }

        fun setNightVisionItems() {
            val active = this@openMainMenu.hasActiveNightVision()

            val name =
                if (active) "gui.main.item.night.enable.name" else "gui.main.item.night.disable.name"
            val interactionType = if (active) InteractionType.ENABLED else InteractionType.DISABLED

            val item = player.basicSmartTransItem(
                key = name,
                descriptionKey = "gui.main.item.night.lore",
                material = Material.ENDER_EYE,
                interactionType = interactionType
            )

            this.setBlockedSlot(InventorySlots.SLOT8ROW1, item) {
                this@openMainMenu.toggleDarkMode()
                setNightVisionItems()
            }
        }

        setNightVisionItems()
        setPhysicItems()
        setBuildItems()
    }
}