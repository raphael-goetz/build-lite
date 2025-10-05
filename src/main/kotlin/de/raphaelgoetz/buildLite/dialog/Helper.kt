package de.raphaelgoetz.buildLite.dialog

import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import net.kyori.adventure.text.Component

fun createDialogBase(
    title: Component,
    externalTitle: Component = title,
    action: DialogBase.DialogAfterAction,
    body: List<DialogBody> = emptyList(),
    inputs: List<DialogInput> = emptyList(),
): DialogBase {
    return DialogBase.create(
        title,
        externalTitle,
        true,
        false,
        action,
        body,
        inputs,
    )
}