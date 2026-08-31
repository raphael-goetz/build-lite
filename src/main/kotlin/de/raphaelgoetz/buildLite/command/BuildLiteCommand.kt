package de.raphaelgoetz.buildLite.command

import de.raphaelgoetz.astralis.command.AstralisCommand
import de.raphaelgoetz.astralis.command.registerCommand
import de.raphaelgoetz.astralis.schedule.doNow
import de.raphaelgoetz.astralis.schedule.doNowAsync
import de.raphaelgoetz.buildLite.dialog.buildtime.showBuildTimeIntervalDialog
import de.raphaelgoetz.buildLite.dialog.home.showCreateHubDialog
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import de.raphaelgoetz.buildLite.dialog.home.showSettingsDialog
import de.raphaelgoetz.buildLite.dialog.review.showReviewCreationDialog
import de.raphaelgoetz.buildLite.dialog.warp.showWarpCreationDialog
import de.raphaelgoetz.buildLite.dialog.world.showWorldCreationDialog
import de.raphaelgoetz.buildLite.dialog.world.showWorldDetailsDialog
import de.raphaelgoetz.buildLite.help.openHelpBook
import de.raphaelgoetz.buildLite.menu.openBannerCreationMenu
import de.raphaelgoetz.buildLite.menu.openBuildTimePlayerPickerMenu
import de.raphaelgoetz.buildLite.menu.openPlayerMenu
import de.raphaelgoetz.buildLite.menu.openReviewMenu
import de.raphaelgoetz.buildLite.menu.openWarpMenu
import de.raphaelgoetz.buildLite.menu.openWorldFolderMenu
import de.raphaelgoetz.buildLite.menu.openWorldMigrationMenu
import de.raphaelgoetz.buildLite.player.getCurrentWorldUUID
import de.raphaelgoetz.buildLite.scoreboard.BuildScoreboard
import de.raphaelgoetz.buildLite.sql.getSqlBuildTimeByWorld
import de.raphaelgoetz.buildLite.sql.getSqlPlayerFavoriteWorldUuids
import de.raphaelgoetz.buildLite.sql.toSqlWorldOrNull
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

fun registerBuildLiteCommand() {
    val root = Commands.literal("buildlite")
        .executes { context -> context.playerExec { showHomeDialog() } }
        .then(Commands.literal("worlds").executes { context -> context.playerExec { openWorldFolderMenu() } })
        .then(Commands.literal("world").executes { context -> context.playerExec { openCurrentWorldDetails() } })
        .then(Commands.literal("warps").executes { context -> context.playerExec { openWarpMenu() } })
        .then(Commands.literal("players").executes { context -> context.playerExec { openPlayerMenu() } })
        .then(Commands.literal("reviews").executes { context ->
            context.playerExec {
                val worldUuid = getCurrentWorldUUID() ?: return@playerExec
                openReviewMenu(worldUuid)
            }
        })
        .then(
            Commands.literal("time")
                .executes { context -> context.playerExec { showBuildTimeIntervalDialog() } }
                .then(
                    Commands.literal("players")
                        .requires { it.hasBuildLitePermission("build-lite.buildtime.admin") }
                        .executes { context -> context.playerExec { openBuildTimePlayerPickerMenu() } }
                )
        )
        .then(
            Commands.literal("create")
                .executes { context -> context.playerExec { showCreateHubDialog() } }
                .then(
                    Commands.literal("world")
                        .requires { it.hasBuildLitePermission("build-lite.world.create") }
                        .executes { context -> context.playerExec { showWorldCreationDialog() } }
                )
                .then(
                    Commands.literal("warp")
                        .requires { it.hasBuildLitePermission("build-lite.warp.create") }
                        .executes { context -> context.playerExec { showWarpCreationDialog() } }
                )
                .then(
                    Commands.literal("review")
                        .requires { it.hasBuildLitePermission("build-lite.review.create") }
                        .executes { context -> context.playerExec { showReviewCreationDialog() } }
                )
                .then(Commands.literal("banner").executes { context ->
                    context.playerExec { openBannerCreationMenu() }
                })
        )
        .then(
            Commands.literal("migrate")
                .requires { it.hasBuildLitePermission("build-lite.world.migrate") }
                .executes { context -> context.playerExec { openWorldMigrationMenu() } }
        )
        .then(Commands.literal("banner").executes { context -> context.playerExec { openBannerCreationMenu() } })
        .then(Commands.literal("activity").executes { context ->
            context.playerExec { showBuildTimeIntervalDialog() }
        })
        .then(Commands.literal("settings").executes { context -> context.playerExec { showSettingsDialog() } })
        .then(Commands.literal("scoreboard").executes { context ->
            context.playerExec { BuildScoreboard.toggle(this) }
        })
        .then(Commands.literal("help").executes { context -> context.playerExec { openHelpBook() } })
        .build()

    registerCommand(
        AstralisCommand(
            command = root,
            description = "Open Build-Lite menus and tools.",
            aliases = listOf("bl"),
        )
    )
}

private fun io.papermc.paper.command.brigadier.CommandSourceStack.hasBuildLitePermission(permission: String): Boolean =
    sender.hasPermission(permission) || sender.hasPermission("build-lite.*")

private fun Player.openCurrentWorldDetails() {
    val worldUuid = getCurrentWorldUUID() ?: return
    val playerUuid = uniqueId

    doNowAsync {
        val record = worldUuid.toString().toSqlWorldOrNull() ?: return@doNowAsync
        val isFavorite = worldUuid in getSqlPlayerFavoriteWorldUuids()
        val buildTime = getSqlBuildTimeByWorld(playerUuid)[worldUuid] ?: 0L

        doNow {
            if (isOnline) showWorldDetailsDialog(record, isFavorite, buildTime)
        }
    }
}

private fun com.mojang.brigadier.context.CommandContext<io.papermc.paper.command.brigadier.CommandSourceStack>.playerExec(
    action: Player.() -> Unit,
): Int {
    val player = source.sender as? Player ?: return 0
    player.action()
    return 1
}
