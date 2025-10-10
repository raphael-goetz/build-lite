package de.raphaelgoetz.buildLite.dialog.review

import de.raphaelgoetz.buildLite.action.actionDeleteReview
import de.raphaelgoetz.buildLite.sql.RecordPlayerReview
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private fun Player.yesAction(record: RecordPlayerReview): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"),
        Component.text("Permanently delete this review"),
        100,
        DialogAction.customClick(
            { _, _ ->
                actionDeleteReview(record)
            }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.noAction(): ActionButton {
    return ActionButton.create(
        Component.text("Discard"), Component.text("Keep this review and close the dialog"), 100, null
    )
}

private fun Player.createReviewDeletionDialog(record: RecordPlayerReview): Dialog {
    val type = DialogType.confirmation(yesAction(record), noAction())
    val base = DialogBase.builder(Component.text("Delete Review")).build()
    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.type(type)
        builder.base(base)
    }
}

fun Player.showReviewDeletionDialog(record: RecordPlayerReview) {
    closeInventory()
    showDialog(createReviewDeletionDialog(record))
}