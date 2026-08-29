package de.raphaelgoetz.buildLite.buildtime

import de.raphaelgoetz.astralis.schedule.doAgain
import de.raphaelgoetz.astralis.schedule.doNowAsync
import de.raphaelgoetz.astralis.schedule.stopRepeatingTask
import de.raphaelgoetz.astralis.schedule.time.TaskTimeTypes
import de.raphaelgoetz.buildLite.cache.PlayerCache
import de.raphaelgoetz.buildLite.cache.isAfk
import de.raphaelgoetz.buildLite.sql.addSqlBuildTimeSeconds
import de.raphaelgoetz.buildLite.sql.BuildTimeCredit
import org.bukkit.Bukkit
import org.bukkit.GameMode
import java.util.UUID

/**
 * Periodically samples online players and credits [tickIntervalSeconds] of
 * build time to whichever plugin-managed world they're actively building in.
 * "Actively building" excludes AFK players, non-creative gamemode, and the
 * home-dialog Build toggle being off (matches cancelWhenBuilder's gate on
 * block placement/breaking).
 */
object BuildTimeTracker {

    private var taskId: UUID? = null

    fun start(afkThresholdSeconds: Long, tickIntervalSeconds: Long) {
        if (taskId != null) return

        val afkThresholdMillis = afkThresholdSeconds * 1000

        taskId = doAgain(period = tickIntervalSeconds, taskTimeTypes = TaskTimeTypes.SECONDS) {
            val credits = Bukkit.getOnlinePlayers().mapNotNull { player ->
                if (player.gameMode != GameMode.CREATIVE) return@mapNotNull null
                if (!PlayerCache.getOrInit(player).recordPlayer.buildMode) return@mapNotNull null
                if (player.isAfk(afkThresholdMillis)) return@mapNotNull null

                // Managed worlds use their database UUID as the Bukkit world
                // name. Parsing it here avoids a synchronous database query on
                // the server thread for every online player, every sample.
                val worldUuid = try {
                    UUID.fromString(player.world.name)
                } catch (_: IllegalArgumentException) {
                    return@mapNotNull null
                }
                BuildTimeCredit(player.uniqueId, worldUuid, tickIntervalSeconds)
            }

            if (credits.isEmpty()) return@doAgain

            doNowAsync {
                addSqlBuildTimeSeconds(credits)
            }
        }
    }

    fun stop() {
        taskId?.let { stopRepeatingTask(it) }
        taskId = null
    }
}
