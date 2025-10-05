package de.raphaelgoetz.buildLite.dialog.credit

import de.raphaelgoetz.buildLite.action.actionAddCredit
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

private fun Player.yesAction(recordWorld: RecordWorld): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"),
        Component.text("Click to confirm to create the world."),
        100,
        DialogAction.customClick(
            { view, _ ->
                val name = view.getText("world_name") ?: return@customClick
                val offlinePlayer = Bukkit.getOfflinePlayer(name)

                println("Raw Name " + offlinePlayer.name)
                println("Raw Name " + offlinePlayer.uniqueId)

                actionAddCredit(offlinePlayer.uniqueId, recordWorld.uniqueId)
            }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.noAction(): ActionButton {
    return ActionButton.create(
        Component.text("Discard"), Component.text("Click to discard the world creation."), 100, null
    )
}

fun Player.createCreditAddDialog(recordWorld: RecordWorld): Dialog {
    val nameInput = DialogInput.text("world_name", Component.text("Name of the Player")).build()

    val base = DialogBase.builder(Component.text("Add Credit"))
        .inputs(listOf(nameInput)).build()
    val type = DialogType.confirmation(yesAction(recordWorld), noAction())

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}