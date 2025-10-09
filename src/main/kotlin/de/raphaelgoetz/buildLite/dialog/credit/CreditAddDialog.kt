package de.raphaelgoetz.buildLite.dialog.credit

import de.raphaelgoetz.buildLite.action.actionAddCredit
import de.raphaelgoetz.buildLite.dialog.world.showWorldEditPropertyDialog
import de.raphaelgoetz.buildLite.sql.RecordWorld
import org.bukkit.Bukkit

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private const val FIELD_NAME_KEY = "credit_name"

private fun Player.yesAction(recordWorld: RecordWorld): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"),
        Component.text("Confirm Changes"),
        100,
        DialogAction.customClick(
            { view, _ ->
                val name = view.getText(FIELD_NAME_KEY) ?: return@customClick
                val offlinePlayer = Bukkit.getOfflinePlayer(name)
                actionAddCredit(offlinePlayer.uniqueId, recordWorld.uniqueId)
            }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.noAction(recordWorld: RecordWorld): ActionButton {
    return ActionButton.create(
        Component.text("Discard"), Component.text("Discard Changes"), 100, DialogAction.customClick(
            { _, _ -> showWorldEditPropertyDialog(recordWorld) },
            ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

fun Player.createCreditAddDialog(recordWorld: RecordWorld): Dialog {
    val nameInput = DialogInput.text(FIELD_NAME_KEY, Component.text("Players Name ")).build()

    val base = DialogBase.builder(Component.text("Add Credit"))
        .inputs(listOf(nameInput)).build()
    val type = DialogType.confirmation(yesAction(recordWorld), noAction(recordWorld))

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}