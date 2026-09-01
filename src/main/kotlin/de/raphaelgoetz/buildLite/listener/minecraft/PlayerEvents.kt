package de.raphaelgoetz.buildLite.listener.minecraft

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.astralis.event.listenCancelled
import de.raphaelgoetz.astralis.schedule.doLater
import de.raphaelgoetz.astralis.schedule.doNow
import de.raphaelgoetz.astralis.schedule.doNowAsync
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.astralis.text.translation.getValue
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.buildLite.action.actionUpdateLastLocation
import de.raphaelgoetz.buildLite.cache.CacheReview
import de.raphaelgoetz.buildLite.cache.PlayerActivityCache
import de.raphaelgoetz.buildLite.cache.PlayerCache
import de.raphaelgoetz.buildLite.cache.markActive
import de.raphaelgoetz.buildLite.dialog.home.FIELD_FLY_SPEED_KEY
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_ACTIONS
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_BUILD_MODE_ACTION
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_CLOSE_ACTION
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_NIGHT_MODE_ACTION
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_REVIEW_MODE_ACTION
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_SIDEBAR_ACTION
import de.raphaelgoetz.buildLite.dialog.home.applySettingsFlySpeed
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import de.raphaelgoetz.buildLite.dialog.home.toggleBuildModePreference
import de.raphaelgoetz.buildLite.dialog.home.toggleBuildSidebarPreference
import de.raphaelgoetz.buildLite.dialog.home.toggleNightModePreference
import de.raphaelgoetz.buildLite.dialog.home.toggleReviewModePreference
import de.raphaelgoetz.buildLite.player.hasWorldEnterPermission
import de.raphaelgoetz.buildLite.buildLiteInstance
import de.raphaelgoetz.buildLite.sql.toSqlWorldOrNull
import de.raphaelgoetz.buildLite.world.OVERWORLD_UUID
import de.raphaelgoetz.buildLite.world.WorldLoader

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
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

    listen<PlayerCustomClickEvent> { event ->
        val player = (event.commonConnection as? PlayerGameConnection)?.player ?: return@listen
        if (event.identifier !in SETTINGS_ACTIONS) return@listen
        player.applySettingsFlySpeed(event.dialogResponseView?.getFloat(FIELD_FLY_SPEED_KEY))
        when (event.identifier) {
            SETTINGS_BUILD_MODE_ACTION -> player.toggleBuildModePreference()
            SETTINGS_NIGHT_MODE_ACTION -> player.toggleNightModePreference()
            SETTINGS_REVIEW_MODE_ACTION -> player.toggleReviewModePreference()
            SETTINGS_SIDEBAR_ACTION -> player.toggleBuildSidebarPreference()
            SETTINGS_CLOSE_ACTION -> {
                player.closeDialog()
                return@listen
            }
            else -> return@listen
        }
    }

    listen<PlayerJoinEvent> { event ->
        val player = event.player
        player.gameMode = GameMode.CREATIVE
        player.markActive()

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

        // DB lookups (player record + last-known-world) happen off the main thread.
        // World creation/teleport and entity visibility touch Bukkit APIs that must
        // run on the main thread, so those are scheduled back via doNow.
        doNowAsync {
            val cachedPlayer = PlayerCache.getOrInit(player)
            val location = cachedPlayer.recordPlayer.lastKnownLocation
            val isOverworld = location?.worldUuid == OVERWORLD_UUID
            val world = if (location != null && !isOverworld) {
                location.worldUuid.toString().toSqlWorldOrNull()
            } else null

            doNow {
                if (!player.isOnline) return@doNow

                // This will teleport the player to his last known location.
                // Only if the match was found. Then the world is probably not existing anymore
                if (location != null && isOverworld) {
                    WorldLoader.lazyTeleportOverworld(location, player)
                } else if (location != null && world != null) {
                    WorldLoader.lazyTeleport(location, world.generator, player)
                } else if (location == null) {
                    player.teleportAsync(buildLiteInstance().spawnLocation)
                }

                // lazyTeleport restores reviews for both already-loaded and
                // newly-loaded worlds. Apply this player's preference only
                // after those display entities exist.
                when (cachedPlayer.recordPlayer.reviewMode) {
                    true -> CacheReview.showAll(player)
                    false -> CacheReview.hideAll(player)
                }
            }
        }
    }

    listen<PlayerQuitEvent> { event ->
        val player = event.player
        val location = player.location
        val world = location.world
        PlayerActivityCache.flush(player.uniqueId)

        doLater(5) {
            if (world.players.isEmpty()) {
                WorldLoader.lazyUnload(world = world)
            }
        }

        PlayerCache.flush(player)
        doNowAsync {
            player.actionUpdateLastLocation(location)
        }

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
        val fromLocation = playerTeleportEvent.from

        // hasWorldEnterPermission (permission check + message) and teleportAsync
        // are all safe to call off the main thread, so only the DB lookup needs
        // to move -- no doNow hop back required here.
        doNowAsync {
            val world = targetWorldName.toSqlWorldOrNull() ?: return@doNowAsync
            if (!player.hasWorldEnterPermission(world.name, world.group)) {
                // If "from" is the same forbidden world as "to" (e.g. on join,
                // when the player's vanilla position and their last-known DB
                // location both already sit in a world they've since lost
                // access to), correcting back to "from" is itself a teleport
                // into that same forbidden world -- which re-triggers this
                // listener and denies again, forever, flooding teleport packets
                // until the player gets kicked. Fall back to a known-safe
                // location instead of chasing a "from" that isn't safe either.
                val destination = if (fromLocation.world.name == targetWorldName) {
                    buildLiteInstance().spawnLocation
                } else {
                    fromLocation
                }

                player.teleportAsync(destination)
            }
        }
    }

    listen<PlayerMoveEvent> { event ->
        val from = event.from
        val to = event.to
        if (from.x != to.x || from.y != to.y || from.z != to.z) {
            event.player.markActive()
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
