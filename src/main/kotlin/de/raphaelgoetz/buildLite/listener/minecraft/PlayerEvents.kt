package de.raphaelgoetz.buildLite.listener.minecraft

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.astralis.event.listenCancelled
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.menu.openMainMenu
import de.raphaelgoetz.buildLite.store.BuildServer
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.*

fun registerPlayerEvents(server: BuildServer) {

    listen<PlayerJoinEvent> { playerJoinEvent ->
        val player = playerJoinEvent.player
        player.gameMode = GameMode.CREATIVE

        val world = Bukkit.getWorld("world")
        if (world != null) player.teleport(world.spawnLocation)

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

        playerJoinEvent.joinMessage(null)
        server.newPlayer(player)
    }

    listen<PlayerQuitEvent> { playerQuitEvent ->
        val player = playerQuitEvent.player
        playerQuitEvent.quitMessage(null)
        player.sendTransText("event.quit.message") {
            type = CommunicationType.ERROR
            resolver = arrayOf(Placeholder.parsed("player", player.name))
        }

        server.removePlayer(player)
    }

    listen<PlayerChangedWorldEvent> { playerChangedWorldEvent ->
        val player = playerChangedWorldEvent.player
        val lastWorld = playerChangedWorldEvent.from

        player.playSound(player, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 1f)

        val actionBar = adventureText(player.locale().getValue("event.change.world.action")) {
            color = Colorization.GREEN
            resolver = arrayOf(Placeholder.parsed("world", player.world.name))
        }

        player.sendActionBar(actionBar)

        if (lastWorld.name == "world") return@listen
        if (lastWorld.players.isNotEmpty()) return@listen
        Bukkit.unloadWorld(lastWorld.name, true)
    }

    listen<EntityDamageEvent> { playerDamageEvent ->
        val entity = playerDamageEvent.entity
        if (entity is Player) playerDamageEvent.isCancelled = true
    }

    listen<PlayerDropItemEvent> { playerDropItemEvent ->
        if (playerDropItemEvent.player.isSneaking) return@listen
        playerDropItemEvent.itemDrop.remove()
    }

    listen<PlayerSwapHandItemsEvent> { playerSwapHandItemsEvent ->
        playerSwapHandItemsEvent.isCancelled = true
        val player = server.asBuildPlayer(playerSwapHandItemsEvent.player) ?: return@listen
        player.openMainMenu(server)
    }

    listen<PlayerTeleportEvent> { playerTeleportEvent ->
        val buildWorld = server.asBuildWorld(playerTeleportEvent.to.world) ?: return@listen
        val enterPermission = buildWorld.permissions
        val player = playerTeleportEvent.player
        val buildPlayer = server.asBuildPlayer(player) ?: return@listen

        for (permission in enterPermission) {

            if (player.hasPermission(permission)) {
                buildPlayer.updateLastLocation(playerTeleportEvent.from)
                return@listen
            }

        }

        player.sendTransText("event.teleport.permission") {
            type = CommunicationType.ERROR
        }

        player.teleport(playerTeleportEvent.from)
    }

    listen<PlayerEggThrowEvent> { event -> event.isHatching = false }

    listenCancelled<PlayerBedEnterEvent>()
    listenCancelled<PlayerFishEvent>()
    listenCancelled<PlayerItemConsumeEvent>()
    listenCancelled<PlayerPortalEvent>()

    listenBuildMode<PlayerArmorStandManipulateEvent>(server)
    listenBuildMode<PlayerBucketEmptyEvent>(server)
    listenBuildMode<PlayerBucketEntityEvent>(server)
    listenBuildMode<PlayerBucketFillEvent>(server)
    listenBuildMode<PlayerInteractEvent>(server)
    listenBuildMode<PlayerInteractEntityEvent>(server)

}

inline fun <reified T : PlayerEvent> listenBuildMode(server: BuildServer) {
    listen<T> { event ->
        val buildPlayer = server.asBuildPlayer(event.player) ?: return@listen
        if (buildPlayer.isBuilding) return@listen
        if (event !is Cancellable) return@listen
        event.isCancelled = true
    }
}