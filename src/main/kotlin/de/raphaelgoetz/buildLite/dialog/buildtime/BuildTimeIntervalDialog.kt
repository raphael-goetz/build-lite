package de.raphaelgoetz.buildLite.dialog.buildtime

import de.raphaelgoetz.buildLite.dialog.createAction
import de.raphaelgoetz.buildLite.menu.openBuildTimeMenu
import de.raphaelgoetz.buildLite.registry.BuildTimeInterval
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.UUID

private const val FIELD_INTERVAL_KEY = "build_time_interval"

/**
 * @param targetUuid whose build time to view. Defaults to the viewer
 * themselves; admins pass another player's uuid/name after picking them
 * from [de.raphaelgoetz.buildLite.menu.openBuildTimePlayerPickerMenu].
 */
fun Player.showBuildTimeIntervalDialog(targetUuid: UUID = uniqueId, targetName: String = name) {
    closeInventory()
    showDialog(createBuildTimeIntervalDialog(targetUuid, targetName))
}

private fun Player.createBuildTimeIntervalDialog(targetUuid: UUID, targetName: String): Dialog {
    val options = BuildTimeInterval.entries.mapIndexed { index, interval ->
        SingleOptionDialogInput.OptionEntry.create(interval.name, Component.text(interval.text), index == 0)
    }
    val intervalInput = DialogInput.singleOption(
        FIELD_INTERVAL_KEY, Component.text("Interval"), options
    ).build()

    val viewAction = createAction("View", "View build time for $targetName.") { view, _ ->
        val selected = view.getText(FIELD_INTERVAL_KEY)?.let { BuildTimeInterval.valueOf(it) }
            ?: BuildTimeInterval.TODAY
        openBuildTimeMenu(targetUuid, targetName, selected)
    }

    val cancelAction = createAction("Cancel", "Close without viewing.") { _, _ -> }

    val base = DialogBase.builder(Component.text("Build Time - $targetName")).inputs(listOf(intervalInput)).build()
    val type = DialogType.multiAction(listOf(viewAction), cancelAction, 1)

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}
