package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.astralis.schedule.doAgainAsync
import de.raphaelgoetz.astralis.schedule.stopRepeatingTask
import de.raphaelgoetz.astralis.schedule.time.TaskTimeTypes
import de.raphaelgoetz.buildLite.config.PluginConfig
import de.raphaelgoetz.buildLite.sql.deleteSqlWorldUpload
import de.raphaelgoetz.buildLite.sql.getExpiredSqlWorldUploads
import org.bukkit.Bukkit
import java.util.UUID

/** Presigned R2 links expire on their own, but the underlying object doesn't
 * -- this periodically deletes objects whose share TTL has passed. */
object ShareCleanupTask {

    private var taskId: UUID? = null

    fun start(config: PluginConfig, intervalMinutes: Long = 5) {
        if (taskId != null || !config.r2Configured) return

        taskId = doAgainAsync(period = intervalMinutes, taskTimeTypes = TaskTimeTypes.MINUTES) {
            for (upload in getExpiredSqlWorldUploads(System.currentTimeMillis())) {
                try {
                    R2Client.delete(config, upload.r2Key)
                } catch (e: Exception) {
                    Bukkit.getLogger().warning("Failed to delete expired R2 upload '${upload.r2Key}': ${e.message}")
                }
                upload.deleteSqlWorldUpload()
            }
        }
    }

    fun stop() {
        taskId?.let { stopRepeatingTask(it) }
        taskId = null
    }
}
