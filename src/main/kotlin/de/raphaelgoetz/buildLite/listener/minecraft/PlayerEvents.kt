package de.raphaelgoetz.buildLite.listener.minecraft

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.astralis.event.listenCancelled
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.buildLite.action.actionUpdateLastLocation
import de.raphaelgoetz.buildLite.cache.PlayerCache
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import de.raphaelgoetz.buildLite.player.hasWorldEnterPermission
import de.raphaelgoetz.buildLite.spawnLocation
import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.world.WorldContainer.worlds
import de.raphaelgoetz.buildLite.world.WorldLoader
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.*

fun registerPlayerEvents() {

    listen<PlayerSwapHandItemsEvent> { event ->
        event.isCancelled = true
        event.player.showHomeDialog()
    }

    listen<PlayerJoinEvent> { event ->
        val player = event.player
        player.gameMode = GameMode.CREATIVE
        val cachedPlayer = PlayerCache.getOrInit(player = event.player)
        val location = cachedPlayer.recordPlayer.lastKnownLocation

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

        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.uniqueId == player.uniqueId) continue
            onlinePlayer.sendTransText("event.join.message") {
                type = CommunicationType.SUCCESS
                resolver = arrayOf(Placeholder.parsed("player", player.name))
            }
        }
        player.sendTransText("event.join.welcome.player") {
            type = CommunicationType.INFO
            resolver = arrayOf(Placeholder.parsed("player", player.name))
            onOpenURL("https://github.com/raphael-goetz/build-lite/issues")
            onHoverText(adventureText(player.locale().getValue("event.join.welcome.player.hover")) {
                type = CommunicationType.DEBUG
            })
        }
        event.joinMessage(null)
    }

    listen<PlayerQuitEvent> { event ->
        //TODO world does not get unloaded
        val player = event.player
        val location = player.location

        println(player.world.players.isEmpty())
        if (player.world.players.isEmpty()) {
            WorldLoader.lazyUnload(world = event.player.world)
        }

        PlayerCache.flush(player)
        player.actionUpdateLastLocation(location)

        event.quitMessage(null)
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendTransText("event.quit.message") {
                type = CommunicationType.ERROR
                resolver = arrayOf(Placeholder.parsed("player", player.name))
            }
        }
    }

    listen<PlayerChangedWorldEvent> { event ->
        val lastWorld = event.from
        if (lastWorld.players.isEmpty()) {
            if (lastWorld.name == "world") return@listen
            WorldLoader.lazyUnload(world = event.from)
        }
    }

    listen<EntityDamageEvent> { playerDamageEvent ->
        val entity = playerDamageEvent.entity
        if (entity is Player) playerDamageEvent.isCancelled = true
    }
    listen<PlayerDropItemEvent> { playerDropItemEvent ->
        if (playerDropItemEvent.player.isSneaking) return@listen
        playerDropItemEvent.itemDrop.remove()
    }

    listen<PlayerTeleportEvent> { playerTeleportEvent ->
        val player = playerTeleportEvent.player
        val targetWorldName = playerTeleportEvent.to.world.name

        //TODO: improve this handling -> async and its separate query
        for (world in worlds) {
            if (world.uniqueId.toString() != targetWorldName) continue
            if (player.hasWorldEnterPermission(world.name, world.group)) continue
            player.sendMessage("You do not have permission to create this world.")
            player.teleportAsync(playerTeleportEvent.from)
            break
        }
    }

    listen<PlayerEggThrowEvent> { event -> event.isHatching = false }
    listenCancelled<PlayerBedEnterEvent>()
    listenCancelled<PlayerFishEvent>()
    listenCancelled<PlayerItemConsumeEvent>()
    listenCancelled<PlayerPortalEvent>()
    listenBuildMode<PlayerArmorStandManipulateEvent>()
    listenBuildMode<PlayerBucketEmptyEvent>()
    listenBuildMode<PlayerBucketEntityEvent>()
    listenBuildMode<PlayerBucketFillEvent>()
    listenBuildMode<PlayerInteractEvent>()
    listenBuildMode<PlayerInteractEntityEvent>()
}

private inline fun <reified T : PlayerEvent> listenBuildMode() {
    listen<T> { event ->
        val cache = PlayerCache.getOrInit(event.player)
        if (cache.recordPlayer.buildMode) return@listen
        if (event !is Cancellable) return@listen
        event.isCancelled = true
    }
}