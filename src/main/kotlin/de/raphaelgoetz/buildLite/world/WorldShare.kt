package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.astralis.schedule.doNow
import de.raphaelgoetz.astralis.schedule.doNowAsync
import de.raphaelgoetz.astralis.text.sendText
import de.raphaelgoetz.astralis.ux.color.Colorization
import de.raphaelgoetz.buildLite.PREFIX
import de.raphaelgoetz.buildLite.buildLiteInstance
import de.raphaelgoetz.buildLite.player.getCurrentWorldUUID
import de.raphaelgoetz.buildLite.sql.createSqlWorldUpload
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.time.Duration
import java.util.UUID

/** Zips the player's current world and uploads it to R2, then messages them
 * a presigned download link. Doesn't unload/reload the world or move the
 * player -- `World.save()` flushes chunks to disk without kicking anyone
 * out, so the world stays live the whole time. */
fun Player.shareCurrentWorld(ttlMinutes: Int) {
    val config = buildLiteInstance().pluginConfig

    if (!config.r2Configured) {
        sendText("$PREFIX World sharing isn't configured on this server.") { color = Colorization.RED }
        return
    }

    val worldUuid = getCurrentWorldUUID() ?: return

    val world = Bukkit.getWorld(worldUuid.toString())
    world?.save()

    sendText("$PREFIX Preparing your download link...") { color = Colorization.YELLOW }

    val playerUuid = uniqueId
    doNowAsync {
        var tempZip: File? = null

        try {
            val folder = WorldStorage.folderFor(worldUuid.toString())
            if (folder == null) {
                doNow { sendShareError(playerUuid, "Could not find that world's files.") }
                return@doNowAsync
            }

            tempZip = File.createTempFile("share-$worldUuid-", ".tar.gz")
            createTarGz(folder, tempZip)

            val key = "shares/$worldUuid/${UUID.randomUUID()}.tar.gz"
            R2Client.upload(config, key, tempZip)

            val ttl = Duration.ofMinutes(ttlMinutes.toLong())
            val link = R2Client.presignGet(config, key, ttl)
            createSqlWorldUpload(key, System.currentTimeMillis() + ttl.toMillis())

            doNow {
                Bukkit.getPlayer(playerUuid)?.sendText(
                    "$PREFIX Your world is ready to share (expires in ${ttlMinutes}m). Click to copy the link:"
                ) {
                    color = Colorization.LIME
                    onCopyClipboard(link)
                }
            }
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Failed to share world '$worldUuid': ${e.message}")
            doNow { sendShareError(playerUuid, "Failed to create a share link.") }
        } finally {
            tempZip?.delete()
        }
    }
}

private fun sendShareError(playerUuid: UUID, message: String) {
    Bukkit.getPlayer(playerUuid)?.sendText("$PREFIX $message") { color = Colorization.RED }
}
