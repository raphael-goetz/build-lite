package de.raphaelgoetz.buildLite.dialog.world

import de.raphaelgoetz.buildLite.action.actionWorldMigrate
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import de.raphaelgoetz.buildLite.sanitiser.getProtectedString
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
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private const val FIELD_NAME_KEY = "migrate_name"
private const val FIELD_GROUP_KEY = "migrate_group"
private const val FIELD_STATE_KEY = "migrate_state"
private const val FIELD_GENERATOR_KEY = "migrate_generator"

private fun Player.yesAction(initialName: String): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"), Component.text("Confirm Migration"), 100, DialogAction.customClick(
            { view, _ ->
                val name = view.getProtectedString(FIELD_NAME_KEY, this)?.sanitiseNameInput() ?: return@customClick
                val group = view.getProtectedString(FIELD_GROUP_KEY, this)?.sanitiseNameInput() ?: return@customClick
                val state = view.getText(FIELD_STATE_KEY)?.toWorldState() ?: return@customClick
                val gen = view.getText(FIELD_GENERATOR_KEY)?.toWorldGenerator() ?: return@customClick
                actionWorldMigrate(initialName, name, group, gen, state)
            }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.noAction(): ActionButton {
    return ActionButton.create(
        Component.text("Discard"), Component.text("Cancel and return to the Home Menu"), 100, DialogAction.customClick(
            { _, _ -> showHomeDialog() },
            ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.createWorldMigrationDialog(initialName: String): Dialog {
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

    val notStartedOption = WorldState.NOT_STARTED.createSingleInput(true)
    val planingOption = WorldState.PLANING.createSingleInput(false)
    val underConstructionOption = WorldState.UNDER_CONSTRUCTION.createSingleInput(false)
    val reviewRequestedOption = WorldState.REVIEW_REQUIRED.createSingleInput(false)
    val finishedOption = WorldState.FINISHED.createSingleInput(false)
    val archivedOption = WorldState.ARCHIVED.createSingleInput(false)

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
    val type = DialogType.confirmation(yesAction(initialName = initialName), noAction())

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}

fun Player.showWorldMigrationDialog(initialName: String) {
    closeInventory()
    showDialog(createWorldMigrationDialog(initialName))
}

private fun WorldState.createSingleInput(initial: Boolean): SingleOptionDialogInput.OptionEntry {
    return SingleOptionDialogInput.OptionEntry.create(
        asDialogInput(), Component.text(text), initial
    )
}