package de.raphaelgoetz.buildLite.dialog

import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.action.DialogAction
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

fun Player.createAction(
    title: String, toolTip: String, width: Int = 100, consumer: (DialogResponseView, Audience) -> Unit
): ActionButton {
    return ActionButton.create(
        Component.text(title), Component.text(toolTip), width, DialogAction.customClick(
            consumer, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}
