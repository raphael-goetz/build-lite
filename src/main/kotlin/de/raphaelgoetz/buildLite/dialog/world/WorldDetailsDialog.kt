package de.raphaelgoetz.buildLite.dialog.world

import de.raphaelgoetz.buildLite.action.actionWorldFavoriteToggle
import de.raphaelgoetz.buildLite.dialog.buildtime.showBuildTimeIntervalDialog
import de.raphaelgoetz.buildLite.dialog.createAction
import de.raphaelgoetz.buildLite.formatting.formatDuration
import de.raphaelgoetz.buildLite.menu.openReviewMenu
import de.raphaelgoetz.buildLite.menu.openWarpMenu
import de.raphaelgoetz.buildLite.menu.openWorldFolderMenu
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.world.WorldLoader
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

fun Player.showWorldDetailsDialog(
    recordWorld: RecordWorld,
    isFavorite: Boolean,
    buildTimeSeconds: Long,
    closeInventoryFirst: Boolean = true,
) {
    if (closeInventoryFirst) closeInventory()
    showDialog(createWorldDetailsDialog(recordWorld, isFavorite, buildTimeSeconds))
}

private fun Player.createWorldDetailsDialog(
    recordWorld: RecordWorld,
    isFavorite: Boolean,
    buildTimeSeconds: Long,
): Dialog {
    val actions = mutableListOf(
        createAction("Join", "Teleport to ${recordWorld.name}.") { _, _ ->
            closeDialog()
            WorldLoader.lazyTeleport(recordWorld.loadableSpawn, recordWorld.generator, this)
        },
        createAction("Warps", "Browse warps in this world.") { _, _ ->
            openWarpMenu(recordWorld.uniqueId)
        },
        createAction("Reviews", "Browse reviews for this world.") { _, _ ->
            openReviewMenu(recordWorld.uniqueId)
        },
        createAction("Build Time", "View your build-time history.") { _, _ ->
            showBuildTimeIntervalDialog(closeInventoryFirst = false)
        },
    )

    if (hasPermission("build-lite.world.favorite") || hasPermission("build-lite.*")) {
        val label = if (isFavorite) "Unpin" else "Pin"
        actions.add(createAction(label, "$label this world in your world browser.") { _, _ ->
            actionWorldFavoriteToggle(recordWorld)
            showWorldDetailsDialog(recordWorld, !isFavorite, buildTimeSeconds, false)
        })
    }

    actions.add(createAction("Quick Actions…", "Open UUID, spawn, and release actions.") { _, _ ->
        showWorldQuickActionsDialog(recordWorld, isFavorite, buildTimeSeconds)
    })

    if (
        hasPermission("build-lite.world.update") ||
        hasPermission("build-lite.world.delete") ||
        hasPermission("build-lite.credit.add") ||
        hasPermission("build-lite.credit.remove") ||
        hasPermission("build-lite.*")
    ) {
        actions.add(createAction("Edit Properties…", "Edit metadata, credits, or delete this world.") { _, _ ->
            showWorldEditPropertyDialog(recordWorld, false)
        })
    }

    val body = DialogBody.plainMessage(
        Component.text("Group: ${recordWorld.group}\n")
            .append(Component.text("Status: ${recordWorld.state.text}\n"))
            .append(Component.text("Generator: ${recordWorld.generator.text}\n"))
            .append(Component.text("Your build time: ${buildTimeSeconds.formatDuration()}"))
    )
    val back = createAction("Back", "Return to the world browser.") { _, _ ->
        openWorldFolderMenu()
    }
    val base = DialogBase.builder(Component.text(recordWorld.name))
        .pause(false)
        .afterAction(DialogBase.DialogAfterAction.NONE)
        .body(listOf(body))
        .build()
    val type = DialogType.multiAction(actions, back, 2)

    return Dialog.create { factory ->
        factory.empty().base(base).type(type)
    }
}
