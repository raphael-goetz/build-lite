package de.raphaelgoetz.buildLite.dialog.world

import de.raphaelgoetz.buildLite.dialog.credit.createCreditAddDialog
import de.raphaelgoetz.buildLite.dialog.credit.createCreditRemoveDialog
import de.raphaelgoetz.buildLite.sql.RecordWorld
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.set.RegistrySet
import de.raphaelgoetz.buildLite.menu.openWorldFolderMenu
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private fun Player.exitAction(): ActionButton {
    return ActionButton.create(
        Component.text("Back"),
        Component.text("Return to the home menu"),
        100,
        DialogAction.customClick(
            { _, _ ->
                openWorldFolderMenu()
            },
            ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.createWorldEditPropertyDialog(recordWorld: RecordWorld): Dialog {
    val base = DialogBase.builder(Component.text("Edit World")).build()
    val set = RegistrySet.valueSet(
        RegistryKey.DIALOG, listOf(
            createWorldUpdateDialog(recordWorld),
            createWorldDeletionDialog(recordWorld),
            createCreditAddDialog(recordWorld),
            createCreditRemoveDialog(recordWorld),
        )
    )

    val type = DialogType.dialogList(set, exitAction(), 1, 256)
    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}

fun Player.showWorldEditPropertyDialog(recordWorld: RecordWorld) {
    closeInventory()
    showDialog(createWorldEditPropertyDialog(recordWorld))
}