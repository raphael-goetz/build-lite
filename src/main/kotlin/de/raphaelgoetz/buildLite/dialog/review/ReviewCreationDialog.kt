package de.raphaelgoetz.buildLite.dialog.review

import de.raphaelgoetz.buildLite.action.actionCreateReview
import de.raphaelgoetz.buildLite.dialog.home.showHomeDialog
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private const val FIELD_DESCRIPTION_KEY = "review_title_input"
private const val FIELD_TITLE_KEY = "review_description_input"

private fun Player.yesAction(): ActionButton {
    return ActionButton.create(
        Component.text("Confirm"), Component.text("Confirm Changes"), 100, DialogAction.customClick(
            { view, _ ->
                val title = view.getText(FIELD_TITLE_KEY) ?: return@customClick
                val description = view.getText(FIELD_DESCRIPTION_KEY) ?: return@customClick

                actionCreateReview(title, description)
            }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

private fun Player.noAction(): ActionButton {
    return ActionButton.create(
        Component.text("Discard"), Component.text("Discard Changes"), 100, DialogAction.customClick(
            { _, _ -> showHomeDialog() },
            ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}

fun Player.createReviewCreationDialog(): Dialog {
    val titleInput = DialogInput.text(FIELD_TITLE_KEY, Component.text("Title")).build()
    val descriptionInput = DialogInput.text(
        FIELD_DESCRIPTION_KEY,
        200,
        Component.text("Review"),
        true,
        "",
        254,
        TextDialogInput.MultilineOptions.create(20, 200)
    )
    val base = DialogBase.builder(Component.text("Add Review")).inputs(listOf(titleInput, descriptionInput)).build()
    val type = DialogType.confirmation(yesAction(), noAction())

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}

fun Player.showReviewCreationDialog() {
    closeInventory()
    showDialog(createReviewCreationDialog())
}