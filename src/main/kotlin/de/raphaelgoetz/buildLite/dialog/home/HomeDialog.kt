package de.raphaelgoetz.buildLite.dialog.home

import de.raphaelgoetz.buildLite.dialog.warp.showWarpCreationDialog
import de.raphaelgoetz.buildLite.dialog.world.showWorldCreationDialog
import de.raphaelgoetz.buildLite.menu.openPlayerMenu
import de.raphaelgoetz.buildLite.menu.openWarpMenu
import de.raphaelgoetz.buildLite.menu.openWorldFolderMenu
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private const val FIELD_BUILD_MODE_KEY = "home_build_mode"
private const val FIELD_NIGHT_MODE_KEY = "home_night_mode"
private const val FIELD_PHYSICS_KEY = "home_physics"
private const val FIELD_SPEED_KEY = "credit_name"

fun Player.showHomeDialog() {
    showDialog(createHomeDialog())
}

private fun Player.createHomeDialog(): Dialog {
    //Inputs
    val buildModeInput = createToggleButton(FIELD_BUILD_MODE_KEY, "Build", true)
    val nightModeInput = createToggleButton(FIELD_NIGHT_MODE_KEY, "Night Vision", true)
    val physicsModeInput = createToggleButton(FIELD_PHYSICS_KEY, "Physics", true)
    val flySpeedInput = DialogInput.numberRange(
        FIELD_SPEED_KEY, 200, Component.text("Speed"), "Fly Speed", 0f, 1f, 0.1f, 0.1f
    )

    //Actions
    val actions = listOf(
        createAction("World Menu", "Click to open the World Menu") { _, _ ->
            openWorldFolderMenu()
        },
        createAction("Create World", "Click to create a new world") { _, _ ->
            showWorldCreationDialog()
        },
        createAction("Warp Menu", "Click to open the Warp Menu") { _, _ ->
            openWarpMenu()
        },
        createAction("Create Warp", "Click to create a new warp") { _, _ ->
            showWarpCreationDialog()
        },
        createAction("Player Menu", "Click to open the Player Menu") { _, _ ->
            openPlayerMenu()
        },
        createAction("Banner Menu", "Click to open the Banner Menu") { _, _ ->
            //TODO
        },
    )

    val closeAction = createAction("Close", "This will open the world Menu") { _, _ -> }

    val base = DialogBase.builder(Component.text("Create a new world"))
        .inputs(listOf(buildModeInput, nightModeInput, physicsModeInput, flySpeedInput)).build()
    val type = DialogType.multiAction(actions, closeAction, 2)

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}

private fun createToggleButton(key: String, label: String, isEnabled: Boolean): SingleOptionDialogInput {
    val trueOption = SingleOptionDialogInput.OptionEntry.create("${key}_enabled", Component.text("Enabled"), isEnabled)
    val falseOption =
        SingleOptionDialogInput.OptionEntry.create("${key}_disabled", Component.text("Disabled"), !isEnabled)
    return DialogInput.singleOption(key, Component.text(label), listOf(trueOption, falseOption)).build()
}

private fun Player.createAction(
    title: String, toolTip: String, consumer: (DialogResponseView, Audience) -> Unit
): ActionButton {
    return ActionButton.create(
        Component.text(title), Component.text(toolTip), 100, DialogAction.customClick(
            consumer, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}
