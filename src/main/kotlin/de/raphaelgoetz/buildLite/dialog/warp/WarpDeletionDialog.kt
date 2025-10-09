package de.raphaelgoetz.buildLite.dialog.warp

import de.raphaelgoetz.buildLite.action.actionWarpDelete
import de.raphaelgoetz.buildLite.sql.RecordPlayerWarp
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private fun Player.yesAction(record: RecordPlayerWarp): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"),
        Component.text("Confirm Changes"),
        100,
        DialogAction.customClick(
            { _, _ ->
                actionWarpDelete(record)
            }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.noAction(): ActionButton {
    return ActionButton.create(
        Component.text("Discard"), Component.text("Discard Changes"), 100, null
    )
}

private fun Player.createWarpDeletionDialog(record: RecordPlayerWarp): Dialog {
    val type = DialogType.confirmation(yesAction(record), noAction())
    val base = DialogBase.builder(Component.text("Delete Warp")).build()
    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.type(type)
        builder.base(base)
    }
}

fun Player.showWarpDeletionDialog(record: RecordPlayerWarp) {
    closeInventory()
    showDialog(createWarpDeletionDialog(record))
}