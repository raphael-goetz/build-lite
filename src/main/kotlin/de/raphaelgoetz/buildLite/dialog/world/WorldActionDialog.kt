package de.raphaelgoetz.buildLite.dialog.world

import de.raphaelgoetz.astralis.text.components.adventureText
import de.raphaelgoetz.buildLite.PREFIX
import de.raphaelgoetz.buildLite.action.actionUpdateWorldSpawn
import de.raphaelgoetz.buildLite.dialog.createAction
import de.raphaelgoetz.buildLite.menu.openWorldFolderMenu
import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.world.WorldLoader
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

fun Player.showWorldActionDialog(recordWorld: RecordWorld) {
    showDialog(createWorldActionDialog(recordWorld))
}

private fun Player.createWorldActionDialog(recordWorld: RecordWorld): Dialog {
    val actions = listOf(
        createAction("Copy UUID", "Click to copy the uuid") { _, _ ->
            sendMessage(adventureText("$PREFIX Click this message to copy the uuid of the world ${recordWorld.name}.") {
                onCopyClipboard(recordWorld.uniqueId.toString())
            })
        },
        createAction("Set World Spawn", "This will update the spawn of this world to your current location") { _,_ ->
            actionUpdateWorldSpawn(recordWorld)
        },
        createAction("Create Release", "This will export the world and make it downloadable") { _,_ ->
            WorldLoader.lazyExport(recordWorld)
        }
    )

    val closeAction = createAction("Back", "This will open the world Menu") { _, _ ->
        openWorldFolderMenu()
    }

    val base = DialogBase.builder(Component.text("World Action Menu")).build()
    val type = DialogType.multiAction(actions, closeAction, 2)

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}