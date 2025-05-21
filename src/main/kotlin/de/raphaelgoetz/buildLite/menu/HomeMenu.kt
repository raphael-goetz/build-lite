package de.raphaelgoetz.buildLite.menu

import de.raphaelgoetz.astralis.items.basicSmartTransItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.items.smartTransItem
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransInventory
import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.SkullMeta

fun BuildPlayer.openMainMenu(buildServer: BuildServer) {

    val player = this.player

    player.openTransInventory("gui.main.title", "Main-Menu", InventoryRows.ROW1) {

        val worldItem = player.basicSmartTransItem(
            key = "gui.main.item.world.name",
            descriptionKey = "gui.main.item.world.lore",
            material = Material.GRASS_BLOCK,
            interactionType = InteractionType.DISPLAY_CLICK
        )

        val playerItem = player.smartTransItem<SkullMeta>(
            key = "gui.player.item.world.name",
            descriptionKey = "gui.player.item.world.lore",
            material = Material.PLAYER_HEAD,
            interactionType = InteractionType.DISPLAY_CLICK
        ) { owningPlayer = player }

        val bannerItem = player.smartTransItem<BannerMeta>(
            key = "gui.main.item.banner.name",
            descriptionKey = "gui.main.item.banner.lore",
            material = Material.GREEN_BANNER,
            interactionType = InteractionType.DISPLAY_CLICK
        ) {
            addPattern(Pattern(DyeColor.BLACK, PatternType.GLOBE))
        }

        this.setBlockedSlot(InventorySlots.SLOT2ROW1, worldItem) {
            this@openMainMenu.openWorldCategoryMenu(buildServer)
        }

        this.setBlockedSlot(InventorySlots.SLOT3ROW1, playerItem) {
            openPlayerOverviewMenu(buildServer)
        }

        this.setBlockedSlot(InventorySlots.SLOT4ROW1, bannerItem) {
            openBannerCreationMenu(buildServer)
        }


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