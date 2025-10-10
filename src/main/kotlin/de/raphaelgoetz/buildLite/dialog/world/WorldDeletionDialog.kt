package de.raphaelgoetz.buildLite.dialog.world

import de.raphaelgoetz.buildLite.action.actionWorldDelete
import de.raphaelgoetz.buildLite.sql.RecordWorld
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private fun Player.yesAction(record: RecordWorld): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"), Component.text("Confirm World Deletion"), 100, DialogAction.customClick(
            { _, _ ->
                actionWorldDelete(record)
            }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}


private fun Player.noAction(recordWorld: RecordWorld): ActionButton {
    return ActionButton.create(
        Component.text("Cancel"), Component.text("Cancel and return to the previous menu"), 100, DialogAction.customClick(
            { _, _ -> showWorldEditPropertyDialog(recordWorld) },
            ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

fun Player.createWorldDeletionDialog(record: RecordWorld): Dialog {
    val body = DialogBody.plainMessage(Component.text("Are you sure you want to permanently delete this world? This action cannot be undone."))
    val type = DialogType.confirmation(yesAction(record), noAction(record))
    val base = DialogBase.builder(Component.text("Delete World")).body(listOf(body)).build()
    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.type(type)
        builder.base(base)
    }
}
