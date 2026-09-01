package de.raphaelgoetz.buildLite.dialog.buildtime

import de.raphaelgoetz.buildLite.PREFIX
import de.raphaelgoetz.buildLite.dialog.createAction
import de.raphaelgoetz.buildLite.menu.openBuildTimeMenu
import de.raphaelgoetz.buildLite.sql.RecordWorld
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.UUID

private const val FIELD_START_DATE_KEY = "build_time_start_date"
private const val FIELD_END_DATE_KEY = "build_time_end_date"

/**
 * @param targetUuid whose build time to view. Defaults to the viewer
 * themselves; admins pass another player's uuid/name after picking them.
 */
fun Player.showBuildTimeIntervalDialog(
    targetUuid: UUID = uniqueId,
    targetName: String = name,
    closeInventoryFirst: Boolean = true,
    worldFilter: RecordWorld? = null,
) {
    if (closeInventoryFirst) closeInventory()

    val defaultRange = defaultBuildTimeDateRange()
    showDialog(
        createBuildTimeIntervalDialog(
            targetUuid,
            targetName,
            worldFilter,
            defaultRange.startDate.toString(),
            defaultRange.endDate.toString(),
        )
    )
}

private fun Player.createBuildTimeIntervalDialog(
    targetUuid: UUID,
    targetName: String,
    worldFilter: RecordWorld?,
    initialStart: String,
    initialEnd: String,
): Dialog {
    val startInput = DialogInput.text(
        FIELD_START_DATE_KEY,
        200,
        Component.text("Start date"),
        true,
        initialStart,
        10,
        null,
    )
    val endInput = DialogInput.text(
        FIELD_END_DATE_KEY,
        200,
        Component.text("End date"),
        true,
        initialEnd,
        10,
        null,
    )

    val viewAction = createAction("View", "View build time for this inclusive date range.") { view, _ ->
        val startText = view.getText(FIELD_START_DATE_KEY) ?: initialStart
        val endText = view.getText(FIELD_END_DATE_KEY) ?: initialEnd
        val range = parseBuildTimeDateRange(startText, endText)

        if (range == null) {
            sendMessage(Component.text("$PREFIX Enter a valid range using YYYY-MM-DD.", NamedTextColor.RED))
            showDialog(createBuildTimeIntervalDialog(targetUuid, targetName, worldFilter, startText, endText))
            return@createAction
        }

        openBuildTimeMenu(targetUuid, targetName, range.startDate, range.endDate, worldFilter)
    }

    val cancelAction = createAction("Cancel", "Close without viewing.") { _, _ -> closeDialog() }
    val body = DialogBody.plainMessage(Component.text("Enter both dates as YYYY-MM-DD. Start and end are included."))
    val title = worldFilter?.let { "Build Time - ${it.name}" } ?: "Build Time - $targetName"
    val base = DialogBase.builder(Component.text(title))
        .pause(false)
        .afterAction(DialogBase.DialogAfterAction.NONE)
        .body(listOf(body))
        .inputs(listOf(startInput, endInput))
        .build()
    val type = DialogType.multiAction(listOf(viewAction), cancelAction, 1)

    return Dialog.create { factory ->
        factory.empty().base(base).type(type)
    }
}
