package de.raphaelgoetz.buildLite.dialog.home

import de.raphaelgoetz.buildLite.dialog.createAction
import de.raphaelgoetz.buildLite.dialog.review.showReviewCreationDialog
import de.raphaelgoetz.buildLite.dialog.warp.showWarpCreationDialog
import de.raphaelgoetz.buildLite.dialog.world.showWorldCreationDialog
import de.raphaelgoetz.buildLite.menu.openBannerCreationMenu
import de.raphaelgoetz.buildLite.menu.openWorldMigrationMenu
import de.raphaelgoetz.buildLite.world.WorldMigrator
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

fun Player.showCreateHubDialog() {
    showDialog(createCreateHubDialog())
}

private fun Player.createCreateHubDialog(): Dialog {
    val actions = mutableListOf<io.papermc.paper.registry.data.dialog.ActionButton>()

    if (hasPermission("build-lite.world.create") || hasPermission("build-lite.*")) {
        actions.add(createAction("World", "Create a new build world.") { _, _ -> showWorldCreationDialog(false) })
    }
    if (hasPermission("build-lite.warp.create") || hasPermission("build-lite.*")) {
        actions.add(createAction("Warp", "Create a warp at your current location.") { _, _ -> showWarpCreationDialog(false) })
    }
    if (hasPermission("build-lite.review.create") || hasPermission("build-lite.*")) {
        actions.add(createAction("Review", "Create a review in the current world.") { _, _ -> showReviewCreationDialog(false) })
    }
    actions.add(createAction("Banner", "Open the banner creation menu.") { _, _ -> openBannerCreationMenu() })
    if (
        (hasPermission("build-lite.world.migrate") || hasPermission("build-lite.*")) &&
        WorldMigrator.detect().isNotEmpty()
    ) {
        actions.add(createAction("Migrate World", "Import an existing world into Build-Lite.") { _, _ ->
            openWorldMigrationMenu()
        })
    }

    val back = createAction("Back", "Return to the home menu.") { _, _ -> showHomeDialog(false) }
    val base = DialogBase.builder(Component.text("Create"))
        .pause(false)
        .afterAction(DialogBase.DialogAfterAction.NONE)
        .build()
    val type = DialogType.multiAction(actions, back, 2)

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}
