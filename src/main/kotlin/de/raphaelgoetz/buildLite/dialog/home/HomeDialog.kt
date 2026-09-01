package de.raphaelgoetz.buildLite.dialog.home

import de.raphaelgoetz.buildLite.dialog.createAction
import de.raphaelgoetz.buildLite.dialog.buildtime.showBuildTimeIntervalDialog
import de.raphaelgoetz.buildLite.help.openHelpBook
import de.raphaelgoetz.buildLite.menu.openPlayerMenu
import de.raphaelgoetz.buildLite.menu.openWarpMenu
import de.raphaelgoetz.buildLite.menu.openWorldFolderMenu
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

fun Player.showHomeDialog(closeInventoryFirst: Boolean = true) {
    if (closeInventoryFirst) closeInventory()
    showDialog(createHomeDialog())
}

private fun Player.createHomeDialog(): Dialog {
    val actions = listOf(
        createAction("Worlds", "Browse and join build worlds.") { _, _ -> openWorldFolderMenu() },
        createAction("Warps", "Browse available warp points.") { _, _ -> openWarpMenu() },
        createAction("Players", "Teleport to another online player.") { _, _ -> openPlayerMenu() },
        createAction("Create…", "Create worlds, warps, reviews, and banners.") { _, _ -> showCreateHubDialog() },
        createAction("My Activity", "View your tracked build time.") { _, _ ->
            showBuildTimeIntervalDialog(closeInventoryFirst = false)
        },
        createAction("Settings", "Configure building and visibility settings.") { _, _ ->
            showSettingsDialog(returnToHome = true)
        },
        createAction("Help", "Open the Build-Lite guide.", 200) { _, _ -> openHelpBook() },
    )

    val close = createAction("Close", "Close the menu.") { _, _ -> closeDialog() }
    val base = DialogBase.builder(Component.text("Build-Lite"))
        .pause(false)
        .afterAction(DialogBase.DialogAfterAction.NONE)
        .build()
    val type = DialogType.multiAction(actions, close, 2)

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}
