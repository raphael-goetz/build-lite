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

private fun Player.yesAction(recordWorld: RecordWorld): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"),
        Component.text("Click to confirm to create the world."),
        100,
        DialogAction.customClick(
            { view, _ ->
                val name = view.getText("world_name")?.sanitiseNameInput()
                val group = view.getText("world_group")?.sanitiseGroupInput()
                val state = view.getText("state_option")?.toWorldState()
                val gen = view.getText("world_generator")?.toWorldGenerator()

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
    val nameInput = DialogInput.text("world_name", 256, Component.text("Name"), true, record.name, 255, null)
    val groupInput = DialogInput.text("world_group", 256, Component.text("Group"), true, record.group, 255, null)

    val voidOption = SingleOptionDialogInput.OptionEntry.create(
        WorldGenerator.VOID.asDialogInput(), Component.text("Void"), record.generator == WorldGenerator.VOID
    )

    val grayOption = SingleOptionDialogInput.OptionEntry.create(
        WorldGenerator.CHESS.asDialogInput(), Component.text("Chessboard"), record.generator == WorldGenerator.CHESS
    )
    val genOption =
        DialogInput.singleOption("world_generator", Component.text("Generator"), listOf(voidOption, grayOption)).build()

    val notStartedOption = SingleOptionDialogInput.OptionEntry.create(
        WorldState.NOT_STARTED.asDialogInput(), Component.text("Not Started"), record.state == WorldState.NOT_STARTED
    )
    val planingOption = SingleOptionDialogInput.OptionEntry.create(
        WorldState.PLANING.asDialogInput(), Component.text("Planing"), record.state == WorldState.PLANING
    )
    val underConstructionOption = SingleOptionDialogInput.OptionEntry.create(
        WorldState.UNDER_CONSTRUCTION.asDialogInput(),
        Component.text("Under Construction"),
        record.state == WorldState.UNDER_CONSTRUCTION
    )
    val reviewRequestedOption = SingleOptionDialogInput.OptionEntry.create(
        WorldState.REVIEW_REQUIRED.asDialogInput(),
        Component.text("Review Requested"),
        record.state == WorldState.REVIEW_REQUIRED
    )
    val finishedOption = SingleOptionDialogInput.OptionEntry.create(
        WorldState.FINISHED.asDialogInput(), Component.text("Finished"), record.state == WorldState.FINISHED
    )
    val archivedOption = SingleOptionDialogInput.OptionEntry.create(
        WorldState.ARCHIVED.asDialogInput(), Component.text("Archived"), record.state == WorldState.ARCHIVED
    )
    val stateOptions = DialogInput.singleOption(
        "state_option", Component.text("State"), listOf(
            notStartedOption,
            planingOption,
            underConstructionOption,
            reviewRequestedOption,
            finishedOption,
            archivedOption,
        )
    ).build()

    val base = DialogBase.builder(Component.text("Edit World"))
        .inputs(listOf(nameInput, groupInput, genOption, stateOptions)).build()
    val type = DialogType.confirmation(yesAction(record), noAction())

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}

