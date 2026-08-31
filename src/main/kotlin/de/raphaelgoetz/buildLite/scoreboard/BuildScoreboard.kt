package de.raphaelgoetz.buildLite.scoreboard

import de.raphaelgoetz.buildLite.PREFIX
import de.raphaelgoetz.buildLite.buildLiteInstance
import de.raphaelgoetz.buildLite.cache.PlayerCache
import de.raphaelgoetz.buildLite.cache.isAfk
import de.raphaelgoetz.buildLite.formatting.formatDuration
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.getSqlBuildTimeByWorld
import de.raphaelgoetz.buildLite.sql.types.WorldState
import de.raphaelgoetz.buildLite.sql.toSqlWorldOrNull
import io.papermc.paper.scoreboard.numbers.NumberFormat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object BuildScoreboard {
    private const val OBJECTIVE_NAME = "build_lite"
    private const val REFRESH_MILLIS = 20_000L
    private const val TOP_BORDER = "<#334155><strikethrough>━━━━━━━━━━━━━━</strikethrough>"
    private const val BOTTOM_BORDER = "<#334155><strikethrough>━━━━━━━━━━━━━</strikethrough>"
    private val miniMessage = MiniMessage.miniMessage()

    private data class Snapshot(
        val world: RecordWorld,
        val todaySeconds: Long,
        val loadedAt: Long,
    )

    private val boards = ConcurrentHashMap<UUID, Scoreboard>()
    private val snapshots = ConcurrentHashMap<Pair<UUID, UUID>, Snapshot>()
    private val pending = ConcurrentHashMap.newKeySet<Pair<UUID, UUID>>()
    private var taskId: Int? = null

    private val preferenceKey: NamespacedKey
        get() = NamespacedKey(buildLiteInstance(), "scoreboard_enabled")

    fun start() {
        if (taskId != null) return
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(buildLiteInstance(), {
            Bukkit.getOnlinePlayers().filter(::isEnabled).forEach(::update)
            boards.keys.removeIf { Bukkit.getPlayer(it) == null }
        }, 20L, 20L)
    }

    fun stop() {
        taskId?.let { Bukkit.getScheduler().cancelTask(it) }
        taskId = null
        Bukkit.getOnlinePlayers().forEach(::restorePreviousDisplay)
        boards.clear()
        snapshots.clear()
        pending.clear()
    }

    fun isEnabled(player: Player): Boolean =
        player.persistentDataContainer.get(preferenceKey, PersistentDataType.BYTE) == 1.toByte()

    fun toggle(player: Player) {
        setEnabled(player, !isEnabled(player))
    }

    fun setEnabled(player: Player, enabled: Boolean) {
        if (enabled == isEnabled(player)) return

        if (!enabled) {
            player.persistentDataContainer.set(preferenceKey, PersistentDataType.BYTE, 0)
            restorePreviousDisplay(player)
        } else {
            if (ensureBoard(player, showConflictMessage = true) == null) return
            player.persistentDataContainer.set(preferenceKey, PersistentDataType.BYTE, 1)
            update(player)
        }
    }

    private fun update(player: Player) {
        val board = ensureBoard(player) ?: return
        val objective = board.getObjective(OBJECTIVE_NAME) ?: return
        board.entries.forEach(board::resetScores)

        val worldUuid = player.world.name.toUuidOrNull()
        if (worldUuid == null) {
            renderLobby(player, objective)
            return
        }

        val key = player.uniqueId to worldUuid
        val snapshot = snapshots[key]
        if (snapshot == null || System.currentTimeMillis() - snapshot.loadedAt >= REFRESH_MILLIS) {
            requestSnapshot(player.uniqueId, worldUuid)
        }

        if (snapshot == null) {
            renderSyncing(player, objective)
            return
        }

        val buildMode = PlayerCache.getOrInit(player).recordPlayer.buildMode
        setLine(objective, 13, TOP_BORDER)
        setLine(objective, 12, "<#155E75>◆ <#22D3EE><bold>WORLD</bold>")
        setLine(objective, 11, "<#F8FAFC><bold>${snapshot.world.name.take(24)}</bold>")
        setLine(objective, 10, "<#94A3B8>Group  <#F8FAFC>${snapshot.world.group.take(20)}")
        setLine(
            objective,
            9,
            "<#94A3B8>Status <#475569>• <${snapshot.world.state.sidebarColor()}>${snapshot.world.state.text}",
        )
        setLine(objective, 8, "")
        setLine(objective, 7, "<#B45309>◆ <#FBBF24><bold>BUILD SESSION</bold>")
        setLine(objective, 6, trackingLine(player, buildMode))
        setLine(objective, 5, "<#94A3B8>Today  <#F8FAFC>${snapshot.todaySeconds.formatDuration()}")
        setLine(objective, 4, positionLine(player))
        setLine(objective, 3, " ")
        setLine(objective, 2, "<#22D3EE>/bl <#475569>• <#94A3B8>Open menu")
        setLine(objective, 1, BOTTOM_BORDER)
    }

    private fun ensureBoard(player: Player, showConflictMessage: Boolean = false): Scoreboard? {
        boards[player.uniqueId]?.let { board ->
            if (player.scoreboard === board) return board
        }

        val manager = Bukkit.getScoreboardManager()
        if (player.scoreboard !== manager.mainScoreboard) {
            if (showConflictMessage) {
                player.sendMessage(Component.text("$PREFIX A different plugin is already using your scoreboard."))
            }
            return null
        }

        val board = manager.newScoreboard
        val title = Component.text("◆ ", TextColor.color(0x155E75))
            .append(Component.text("BUILD", TextColor.color(0x22D3EE), TextDecoration.BOLD))
            .append(Component.text(" LITE", TextColor.color(0xFBBF24), TextDecoration.BOLD))
            .append(Component.text(" ◆", TextColor.color(0x155E75)))
        val objective = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, title)
        objective.numberFormat(NumberFormat.blank())
        objective.displaySlot = DisplaySlot.SIDEBAR
        boards[player.uniqueId] = board
        player.scoreboard = board
        return board
    }

    private fun requestSnapshot(playerUuid: UUID, worldUuid: UUID) {
        val key = playerUuid to worldUuid
        if (!pending.add(key)) return

        Bukkit.getScheduler().runTaskAsynchronously(buildLiteInstance(), Runnable {
            try {
                val world = worldUuid.toString().toSqlWorldOrNull() ?: return@Runnable
                val time = getSqlBuildTimeByWorld(playerUuid, LocalDate.now())[worldUuid] ?: 0L
                snapshots[key] = Snapshot(world, time, System.currentTimeMillis())
            } finally {
                pending.remove(key)
            }
        })
    }

    private fun restorePreviousDisplay(player: Player) {
        val board = boards.remove(player.uniqueId) ?: return
        if (player.scoreboard === board) {
            player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        }
    }

    private fun setLine(objective: org.bukkit.scoreboard.Objective, score: Int, value: String) {
        objective.getScore("build_lite_line_$score").apply {
            this.score = score
            customName(miniMessage.deserialize(value))
        }
    }

    private fun renderLobby(player: Player, objective: org.bukkit.scoreboard.Objective) {
        setLine(objective, 11, TOP_BORDER)
        setLine(objective, 10, "<#155E75>◆ <#22D3EE><bold>BUILD HUB</bold>")
        setLine(objective, 9, "<#94A3B8>Welcome, <#F8FAFC>${player.name.take(20)}")
        setLine(objective, 8, "<#94A3B8>Online  <#4ADE80>${Bukkit.getOnlinePlayers().size} builders")
        setLine(objective, 7, "")
        setLine(objective, 6, "<#B45309>◆ <#FBBF24><bold>QUICK START</bold>")
        setLine(objective, 5, "<#94A3B8>Choose a world and")
        setLine(objective, 4, "<#94A3B8>start creating.")
        setLine(objective, 3, " ")
        setLine(objective, 2, "<#22D3EE>/bl worlds <#475569>• <#94A3B8>Browse")
        setLine(objective, 1, BOTTOM_BORDER)
    }

    private fun renderSyncing(player: Player, objective: org.bukkit.scoreboard.Objective) {
        val buildMode = PlayerCache.getOrInit(player).recordPlayer.buildMode
        setLine(objective, 9, TOP_BORDER)
        setLine(objective, 8, "<#155E75>◆ <#22D3EE><bold>BUILD WORLD</bold>")
        setLine(objective, 7, "<#94A3B8>Syncing world data…")
        setLine(objective, 6, "")
        setLine(objective, 5, "<#B45309>◆ <#FBBF24><bold>BUILD SESSION</bold>")
        setLine(objective, 4, trackingLine(player, buildMode))
        setLine(objective, 3, positionLine(player))
        setLine(objective, 2, "<#22D3EE>/bl <#475569>• <#94A3B8>Open menu")
        setLine(objective, 1, BOTTOM_BORDER)
    }

    private fun trackingLine(player: Player, buildMode: Boolean): String {
        if (player.gameMode != GameMode.CREATIVE) {
            return "<#FBBF24>● <#F8FAFC>Paused <#475569>• <#94A3B8>Creative only"
        }
        if (!buildMode) return "<#FB7185>● <#F8FAFC>Paused <#475569>• <#94A3B8>Build mode off"

        val afkThreshold = buildLiteInstance().config.getLong("build-time.afkThresholdSeconds", 180) * 1_000
        if (player.isAfk(afkThreshold)) return "<#F59E0B>● <#F8FAFC>AFK <#475569>• <#94A3B8>Time paused"

        return "<#4ADE80>● <#F8FAFC>Tracking build time"
    }

    private fun positionLine(player: Player): String =
        "<#94A3B8>XYZ  <#F8FAFC>${player.location.blockX} <#475569>/ " +
            "<#F8FAFC>${player.location.blockY} <#475569>/ <#F8FAFC>${player.location.blockZ}"

    private fun WorldState.sidebarColor(): String = when (this) {
        WorldState.NOT_STARTED -> "#94A3B8"
        WorldState.PLANING -> "#38BDF8"
        WorldState.UNDER_CONSTRUCTION -> "#FBBF24"
        WorldState.REVIEW_REQUIRED -> "#C084FC"
        WorldState.FINISHED -> "#4ADE80"
        WorldState.ARCHIVED -> "#64748B"
    }

    private fun String.toUuidOrNull(): UUID? = try {
        UUID.fromString(this)
    } catch (_: IllegalArgumentException) {
        null
    }
}
