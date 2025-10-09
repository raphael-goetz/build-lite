package de.raphaelgoetz.buildLite.dialog.world

import de.raphaelgoetz.buildLite.action.actionWorldCreate
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.WorldState
import de.raphaelgoetz.buildLite.sql.types.asDialogInput
import de.raphaelgoetz.buildLite.sql.types.toWorldGenerator
import de.raphaelgoetz.buildLite.sql.types.toWorldState
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private const val FIELD_NAME_KEY = "create_name"
private const val FIELD_GROUP_KEY = "create_group"
private const val FIELD_STATE_KEY = "create_state"
private const val FIELD_GENERATOR_KEY = "create_generator"

private fun Player.yesAction(): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"), Component.text("Confirm Changes"), 100, DialogAction.customClick(
            { view, _ ->
                val name = view.getText(FIELD_NAME_KEY)
                val group = view.getText(FIELD_GROUP_KEY)
                val state = view.getText(FIELD_STATE_KEY)?.toWorldState()
                val gen = view.getText(FIELD_GENERATOR_KEY)?.toWorldGenerator()

                if (name == null || group == null || state == null || gen == null) {
                    return@customClick
                }

                actionWorldCreate(name, group, gen, state)
            }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.noAction(): ActionButton {
    return ActionButton.create(
        Component.text("Discard"), Component.text("Discard Changes"), 100, DialogAction.customClick(
            { _, _ -> showHomeDialog() },
            ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.createWorldCreationDialog(): Dialog {
    val nameInput = DialogInput.text(FIELD_NAME_KEY, Component.text("Name")).build()
    val groupInput = DialogInput.text(FIELD_GROUP_KEY, Component.text("Group")).build()

    val voidOption =
        SingleOptionDialogInput.OptionEntry.create(WorldGenerator.VOID.asDialogInput(), Component.text("Void"), true)
    val grayOption = SingleOptionDialogInput.OptionEntry.create(
        WorldGenerator.CHESS.asDialogInput(), Component.text("Chessboard"), false
    )
    val genOption =
        DialogInput.singleOption(FIELD_GENERATOR_KEY, Component.text("Generator"), listOf(voidOption, grayOption))
            .build()

    val notStartedOption = SingleOptionDialogInput.OptionEntry.create(
        WorldState.NOT_STARTED.asDialogInput(), Component.text("Not Started"), true
    )
    val planingOption =
        SingleOptionDialogInput.OptionEntry.create(WorldState.PLANING.asDialogInput(), Component.text("Planing"), false)
    val underConstructionOption = SingleOptionDialogInput.OptionEntry.create(
        WorldState.UNDER_CONSTRUCTION.asDialogInput(), Component.text("Under Construction"), false
    )
    val reviewRequestedOption = SingleOptionDialogInput.OptionEntry.create(
        WorldState.REVIEW_REQUIRED.asDialogInput(), Component.text("Review Requested"), false
    )
    val finishedOption = SingleOptionDialogInput.OptionEntry.create(
        WorldState.FINISHED.asDialogInput(), Component.text("Finished"), false
    )
    val archivedOption = SingleOptionDialogInput.OptionEntry.create(
        WorldState.ARCHIVED.asDialogInput(), Component.text("Archived"), false
    )
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
    val body = DialogBody.plainMessage(Component.text("Only letters (A–Z, a–z) and numbers are allowed. Your input will automatically be converted to lowercase when saved."))

    val base = DialogBase.builder(Component.text("Create World"))
        .inputs(listOf(nameInput, groupInput, genOption, stateOptions)).body(listOf(body)).build()
    val type = DialogType.confirmation(yesAction(), noAction())

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}

fun Player.showWorldCreationDialog() {
    closeInventory()
    showDialog(createWorldCreationDialog())
}