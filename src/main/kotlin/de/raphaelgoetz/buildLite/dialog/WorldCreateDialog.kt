package de.raphaelgoetz.buildLite.dialog

import de.raphaelgoetz.astralis.schedule.doNow
import de.raphaelgoetz.astralis.text.communication.CommunicationType
import de.raphaelgoetz.astralis.text.translation.sendTransText
import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

fun BuildPlayer.openWorldCreationDialog(server: BuildServer) {

    val nameInput = DialogInput.text("world_name", Component.text("Name")).build()
    val groupInput = DialogInput.text("world_group", Component.text("Group")).build()

    val voidOption = SingleOptionDialogInput.OptionEntry.create("void_option", Component.text("Void"), true)
    val grayOption = SingleOptionDialogInput.OptionEntry.create("gray_option", Component.text("Gray 16x16"), false)
    val genOption = DialogInput.singleOption("world_generator", Component.text("Generator"), listOf(voidOption, grayOption)).build()

    val base = DialogBase.builder(Component.text("Create a new world")).inputs(listOf(nameInput, groupInput, genOption)).build()

    val menu = Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(DialogType.confirmation(
            ActionButton.create(
                Component.text("Confirm", TextColor.color(0xAEFFC1)),
                Component.text("Click to confirm your input."),
                100,
                DialogAction.customClick(
                    DialogActionCallback { view, audience ->
                        val player = audience as? Player ?: return@DialogActionCallback
                        val name = view.getText("world_name")
                        val group = view.getText("world_group")

                        doNow {
                            if (name == null || group == null) {
                                return@doNow
                            }

                            server.createWorld(name, group)

                            player.sendTransText("question.world.create.success") {
                                type = CommunicationType.SUCCESS
                            }
                        }
                    },
                    ClickCallback.Options.builder()
                        .uses(1)
                        .lifetime(ClickCallback.DEFAULT_LIFETIME)
                        .build()
                )
            ),
            ActionButton.create(
                Component.text("Discard", TextColor.color(0xFFA0B1)),
                Component.text("Click to discard your input."),
                100,
                null // If we set the action to null, it doesn't do anything and closes the dialog
            )
        ))
    }

    player.showDialog(menu)
}
