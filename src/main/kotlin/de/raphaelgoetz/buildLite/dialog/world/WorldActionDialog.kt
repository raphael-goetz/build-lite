package de.raphaelgoetz.buildLite.dialog.world

import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.buildLite.PREFIX
import de.raphaelgoetz.buildLite.action.actionUpdateWorldSpawn
import de.raphaelgoetz.buildLite.dialog.createAction
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.world.WorldLoader
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

fun Player.showWorldQuickActionsDialog(recordWorld: RecordWorld, isFavorite: Boolean, buildTimeSeconds: Long) {
    showDialog(createWorldQuickActionsDialog(recordWorld, isFavorite, buildTimeSeconds))
}

private fun Player.createWorldQuickActionsDialog(
    recordWorld: RecordWorld,
    isFavorite: Boolean,
    buildTimeSeconds: Long,
): Dialog {
    val actions = mutableListOf(
        createAction("Copy UUID", "Click to copy this world's unique identifier") { _, _ ->
            sendMessage(adventureText("$PREFIX Click this message to copy the UUID for world ${recordWorld.name}.") {
                onCopyClipboard(recordWorld.uniqueId.toString())
            })
        },
        createAction("Create Release", "Export this world and make it available for download") { _,_ ->
            WorldLoader.lazyExport(recordWorld)
        }
    )

    if (hasPermission("build-lite.world.update") || hasPermission("build-lite.*")) {
        actions.add(1, createAction("Set World Spawn", "Set the world's spawn point to your current location") { _,_ ->
            actionUpdateWorldSpawn(recordWorld)
        })
    }

    val closeAction = createAction("Back", "Return to world details") { _, _ ->
        showWorldDetailsDialog(recordWorld, isFavorite, buildTimeSeconds, false)
    }

    val base = DialogBase.builder(Component.text("Quick Actions - ${recordWorld.name}"))
        .pause(false)
        .afterAction(DialogBase.DialogAfterAction.NONE)
        .build()
    val type = DialogType.multiAction(actions, closeAction, 2)

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}
