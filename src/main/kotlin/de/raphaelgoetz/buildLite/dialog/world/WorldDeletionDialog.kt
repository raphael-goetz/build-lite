package de.raphaelgoetz.buildLite.dialog.world

import de.raphaelgoetz.buildLite.action.actionWorldDelete
import de.raphaelgoetz.buildLite.sql.RecordWorld
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private fun Player.yesAction(record: RecordWorld): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"),
        Component.text("Click to confirm to delete this world."),
        100,
        DialogAction.customClick(
            { _, _ ->
                actionWorldDelete(record)
            }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.noAction(): ActionButton {
    return ActionButton.create(
        Component.text("Discard"), Component.text("Click to discard the world creation."), 100, null
    )
}

fun Player.createWorldDeletionDialog(record: RecordWorld): Dialog {
    val type = DialogType.confirmation(yesAction(record), noAction())
    val base = DialogBase.builder(Component.text("Delete a new world")).build()
    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.type(type)
        builder.base(base)
    }
}
