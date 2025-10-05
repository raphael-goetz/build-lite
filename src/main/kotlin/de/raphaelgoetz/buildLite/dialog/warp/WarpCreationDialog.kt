package de.raphaelgoetz.buildLite.dialog.warp

import de.raphaelgoetz.buildLite.action.actionWarpCreate
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private fun Player.yesAction(): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"),
        Component.text("Click to confirm to create the world."),
        100,
        DialogAction.customClick(
            { view, _ ->
                val name = view.getText("warp_name")
                val isPrivate = view.getBoolean("warp_private")

                if (name == null || isPrivate == null) {
                    return@customClick
                }

                actionWarpCreate(name, isPrivate)
            }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.noAction(): ActionButton {
    return ActionButton.create(
        Component.text("Discard"), Component.text("Click to discard the world creation."), 100, null
    )
}

private fun Player.createWarpCreationDialog(): Dialog {
    val nameInput = DialogInput.text("warp_name", Component.text("Warp Name")).build()
    val isPrivate = DialogInput.bool("warp_private", Component.text("Is Private")).build()

    val base = DialogBase.builder(Component.text("Create a new warp")).inputs(listOf(nameInput, isPrivate)).build()
    val type = DialogType.confirmation(yesAction(), noAction())

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}

fun Player.showWarpCreationDialog() {
    closeDialog()
    closeInventory()
    showDialog(createWarpCreationDialog())
}