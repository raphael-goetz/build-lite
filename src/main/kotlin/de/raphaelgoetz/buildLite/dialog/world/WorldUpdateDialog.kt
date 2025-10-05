package de.raphaelgoetz.buildLite.dialog.world

import de.raphaelgoetz.buildLite.action.actionWorldUpdate
import de.raphaelgoetz.buildLite.sanitiser.sanitiseGroupInput
import de.raphaelgoetz.buildLite.sanitiser.sanitiseNameInput
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.WorldState
import de.raphaelgoetz.buildLite.sql.types.asDialogInput
import de.raphaelgoetz.buildLite.sql.types.toWorldGenerator
import de.raphaelgoetz.buildLite.sql.types.toWorldState

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private const val FIELD_NAME_KEY = "home_build_mode"
private const val FIELD_GROUP_KEY = "home_night_mode"
private const val FIELD_STATE_KEY = "home_physics"
private const val FIELD_GENERATOR_KEY = "credit_name"

private fun Player.yesAction(recordWorld: RecordWorld): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"),
        Component.text("Click to confirm to create the world."),
        100,
        DialogAction.customClick(
            { view, _ ->
                val name = view.getText(FIELD_NAME_KEY)?.sanitiseNameInput()
                val group = view.getText(FIELD_GROUP_KEY)?.sanitiseGroupInput()
                val state = view.getText(FIELD_STATE_KEY)?.toWorldState()
                val gen = view.getText(FIELD_GENERATOR_KEY)?.toWorldGenerator()

                actionWorldUpdate(recordWorld, name, group, generator = gen, state = state)
            }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.noAction(): ActionButton {
    return ActionButton.create(
        Component.text("Discard"), Component.text("Click to discard the world creation."), 100, null
    )
}

fun Player.createWorldUpdateDialog(record: RecordWorld): Dialog {
    val nameInput = DialogInput.text(FIELD_NAME_KEY, 200, Component.text("Name"), true, record.name, 255, null)
    val groupInput = DialogInput.text("world_group", 200, Component.text("Group"), true, record.group, 255, null)

    val grayOption = WorldGenerator.CHESS.createSingleInput(record)
    val voidOption = WorldGenerator.VOID.createSingleInput(record)
    val genOption =
        DialogInput.singleOption(FIELD_GENERATOR_KEY, Component.text("Generator"), listOf(voidOption, grayOption))
            .build()

    val notStartedOption = WorldState.NOT_STARTED.createSingleInput(record)
    val planingOption = WorldState.PLANING.createSingleInput(record)
    val underConstructionOption = WorldState.UNDER_CONSTRUCTION.createSingleInput(record)
    val reviewRequestedOption = WorldState.REVIEW_REQUIRED.createSingleInput(record)
    val finishedOption = WorldState.FINISHED.createSingleInput(record)
    val archivedOption = WorldState.ARCHIVED.createSingleInput(record)

    val stateOptions = DialogInput.singleOption(
        FIELD_STATE_KEY, Component.text("State"), listOf(
            notStartedOption,
            planingOption,
            underConstructionOption,
            reviewRequestedOption,
            finishedOption,
            archivedOption,
        )
    ).build()

    val base =
        DialogBase.builder(Component.text("Edit World")).inputs(listOf(nameInput, groupInput, genOption, stateOptions))
            .build()
    val type = DialogType.confirmation(yesAction(record), noAction())

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}

private fun WorldState.createSingleInput(record: RecordWorld): SingleOptionDialogInput.OptionEntry {
    return SingleOptionDialogInput.OptionEntry.create(
        asDialogInput(), Component.text(text), record.state == this
    )
}

private fun WorldGenerator.createSingleInput(record: RecordWorld): SingleOptionDialogInput.OptionEntry {
    return SingleOptionDialogInput.OptionEntry.create(
        asDialogInput(), Component.text(text), record.generator == this
    )
}