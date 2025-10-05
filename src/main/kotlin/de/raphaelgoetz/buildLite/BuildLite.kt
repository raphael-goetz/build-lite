package de.raphaelgoetz.buildLite

import de.raphaelgoetz.astralis.Astralis
import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.astralis.items.builder.SmartItem
import de.raphaelgoetz.astralis.items.createSmartItem
import de.raphaelgoetz.astralis.items.data.InteractionType
import de.raphaelgoetz.astralis.text.components.RenderMode
import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.ui.builder.SmartClick
import de.raphaelgoetz.astralis.ui.data.InventoryRows
import de.raphaelgoetz.astralis.ui.data.InventorySlots
import de.raphaelgoetz.astralis.ui.openTransPageInventory
import de.raphaelgoetz.astralis.world.generator.VoidGenerator
import de.raphaelgoetz.buildLite.BuildServer.spawnLocation
import de.raphaelgoetz.buildLite.action.actionUpdateLastLocation
import de.raphaelgoetz.buildLite.dialog.credit.createCreditAddDialog
import de.raphaelgoetz.buildLite.dialog.credit.createCreditRemoveDialog
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import de.raphaelgoetz.buildLite.dialog.world.createWorldUpdateDialog
import de.raphaelgoetz.buildLite.item.createPageLeftItem
import de.raphaelgoetz.buildLite.item.createPageRightItem
import de.raphaelgoetz.buildLite.item.createPlayerHead
import de.raphaelgoetz.buildLite.menu.openWorldFolderMenu
import de.raphaelgoetz.buildLite.sql.SqlPlayer
import de.raphaelgoetz.buildLite.sql.SqlPlayerCredit
import de.raphaelgoetz.buildLite.sql.SqlPlayerFavorite
import de.raphaelgoetz.buildLite.sql.SqlPlayerWarp
import de.raphaelgoetz.buildLite.sql.SqlWorld
import de.raphaelgoetz.buildLite.sql.initSqlPlayer
import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.toGenerator
import de.raphaelgoetz.buildLite.world.WorldContainer
import de.raphaelgoetz.buildLite.world.WorldContainer.worlds
import de.raphaelgoetz.buildLite.world.WorldLoader
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogInstancesProvider
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor.color
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Registry
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class BuildLite : Astralis() {
    override fun enable() {

        Database.connect(
            url = "jdbc:sqlite:worlds.db", // path to your SQLite file
            driver = "org.sqlite.JDBC"
        )
        transaction {
            SchemaUtils.create(
                SqlPlayer, SqlPlayerCredit, SqlPlayerFavorite, SqlPlayerWarp, SqlWorld
            )
        }

        listen<PlayerSwapHandItemsEvent> { event ->
            event.isCancelled = true

            val items = mutableListOf<SmartClick>()
            for (i in 0..200) {
                val item = createSmartItem<ItemMeta>(
                    i.toString(),
                    Material.STONE,
                    i.toString(),
                    listOf(),
                    InteractionType.DISPLAY_CLICK
                )

                val int = SmartClick(item) {

                }

                items.add(int)

            }


            event.player.openTransPageInventory(
                "Test",
                "Test",
                InventoryRows.ROW6,
                items,
                InventorySlots.SLOT1ROW1,
                InventorySlots.SLOT9ROW5
            ) {
                pageLeft(InventorySlots.SLOT1ROW6, event.player.createPageLeftItem())
                pageRight(InventorySlots.SLOT9ROW6, event.player.createPageRightItem())
            }
            //event.player.showHomeDialog()
        }

        listen<PlayerJoinEvent> { event ->
            val location = event.player.initSqlPlayer().lastKnownLocation

            // This will teleport the player to his last known location
            location?.let {
                var generator: WorldGenerator? = null
                for (world in worlds) {
                    if (location.worldUuid == world.uniqueId) {
                        generator = world.generator
                        break
                    }
                }

                //Only if the match was found. Then the world is probably not existing anymore
                generator?.let {
                    WorldLoader.lazyTeleport(location, it, event.player)
                }
            }

            if (location == null) {
                event.player.teleportAsync(spawnLocation)
            }
        }

        listen<PlayerQuitEvent> { event ->
            val player = event.player
            val location = player.location
            if (player.world.players.isEmpty()) {
                println("world wasx empty")
                WorldLoader.lazyUnload(world = event.player.world)
            }

            player.actionUpdateLastLocation(location)
        }

        listen<PlayerChangedWorldEvent> { event ->
            println(event.from.players.size)
            if (event.from.players.isEmpty()) {
                println("world wasx empty")
                WorldLoader.lazyUnload(world = event.from)
            }
        }

        // val buildServer = BuildServer()
        // buildServer.registerCommands()
        // buildServer.registerListener()
    }
}

fun String.capitalizeFirst(): String = this.lowercase().replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}
