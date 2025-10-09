package de.raphaelgoetz.buildLite.dialog.home

import de.raphaelgoetz.buildLite.action.actionDisableBuildMode
import de.raphaelgoetz.buildLite.action.actionDisableNightMode
import de.raphaelgoetz.buildLite.action.actionDisableReviewMode
import de.raphaelgoetz.buildLite.action.actionEnableBuildMode
import de.raphaelgoetz.buildLite.action.actionEnableNightMode
import de.raphaelgoetz.buildLite.action.actionEnableReviewMode
import de.raphaelgoetz.buildLite.cache.PlayerCache
import de.raphaelgoetz.buildLite.dialog.createAction
import de.raphaelgoetz.buildLite.dialog.review.showReviewCreationDialog
import de.raphaelgoetz.buildLite.dialog.warp.showWarpCreationDialog
import de.raphaelgoetz.buildLite.dialog.world.showWorldCreationDialog
import de.raphaelgoetz.buildLite.menu.openBannerCreationMenu
import de.raphaelgoetz.buildLite.menu.openPlayerMenu
import de.raphaelgoetz.buildLite.menu.openWarpMenu
import de.raphaelgoetz.buildLite.menu.openWorldFolderMenu
import de.raphaelgoetz.buildLite.menu.openWorldMigrationMenu
import de.raphaelgoetz.buildLite.world.WorldMigrator
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

private const val FIELD_BUILD_MODE_KEY = "home_build_mode"
private const val FIELD_NIGHT_MODE_KEY = "home_night_mode"
private const val FIELD_REVIEW_MODE_KEY = "home_review_mode"
private const val FIELD_SPEED_KEY = "home_speed"

fun Player.showHomeDialog() {
    closeInventory()
    showDialog(createHomeDialog())
}

private fun Player.createHomeDialog(): Dialog {
    val cachedPlayer = PlayerCache.getOrInit(this)
    val hasMigrations = WorldMigrator.detect().isNotEmpty()

    //Inputs
    val buildModeInput = createToggleButton(FIELD_BUILD_MODE_KEY, "Build", cachedPlayer.recordPlayer.buildMode)
    val nightModeInput = createToggleButton(FIELD_NIGHT_MODE_KEY, "Night Vision", cachedPlayer.recordPlayer.nightMode)
    val reviewModeInput = createToggleButton(FIELD_REVIEW_MODE_KEY, "Reviews", cachedPlayer.recordPlayer.reviewMode)
    val flySpeedInput = DialogInput.numberRange(
        FIELD_SPEED_KEY, 200, Component.text("Speed"), "%s (Current Speed: %s)", 0.1f, 1f, this.flySpeed, 0.1f
    )

    //Actions
    val actions = mutableListOf(
        createAction("Banner Menu", "Click to open the Banner Menu") { _, _ ->
            openBannerCreationMenu()
        },
        createAction("Create World", "Click to create a new world") { _, _ ->
            showWorldCreationDialog()
        },
        createAction("Warp Menu", "Click to open the Warp Menu") { _, _ ->
            openWarpMenu()
        },
        createAction("Create Warp", "Click to create a new warp") { _, _ ->
            showWarpCreationDialog()
        },
        createAction("Player Menu", "Click to open the Player Menu") { _, _ ->
            openPlayerMenu()
        },
        createAction("Create Review", "Click to open the Banner Menu") { _, _ ->
            showReviewCreationDialog()
        },
        createAction("World Menu", "Click to open the World Menu", if (hasMigrations) 100 else 200) { _, _ ->
            openWorldFolderMenu()
        },
    )

    if (hasMigrations) {
        actions.add(createAction("Migrate Worlds", "Click to open the Migration Menu", 100) { _, _ ->
            openWorldMigrationMenu()
        })
    }

    val closeAction = createAction("Close", "This will open the world Menu") { view, _ ->
        val speed = view.getFloat(FIELD_SPEED_KEY)
        val nightMode = view.getText(FIELD_NIGHT_MODE_KEY)
        val buildMode= view.getText(FIELD_BUILD_MODE_KEY)
        val reviewMode = view.getText(FIELD_REVIEW_MODE_KEY)

        speed?.let { value -> this.flySpeed = value  }
        buildMode?.let { value ->
            when (value) {
                "home_build_mode_disabled" -> this.actionDisableBuildMode()
                "home_build_mode_enabled" -> this.actionEnableBuildMode()
            }
        }

        nightMode?.let { value ->
            when (value) {
                "home_night_mode_disabled" -> this.actionDisableNightMode()
                "home_night_mode_enabled" -> this.actionEnableNightMode()
            }
        }

        reviewMode?.let { value ->
            when (value) {
                "home_review_mode_disabled" -> this.actionDisableReviewMode()
                "home_review_mode_enabled" -> this.actionEnableReviewMode()
            }
        }
    }

    val base = DialogBase.builder(Component.text("Home Menu"))
        .inputs(listOf(buildModeInput, nightModeInput, reviewModeInput, flySpeedInput)).build()
    val type = DialogType.multiAction(actions, closeAction, 2)

    return Dialog.create { factory ->
        val builder = factory.empty()
        builder.base(base)
        builder.type(type)
    }
}

private fun createToggleButton(key: String, label: String, isEnabled: Boolean): SingleOptionDialogInput {
    val trueOption = SingleOptionDialogInput.OptionEntry.create("${key}_enabled", Component.text("Enabled"), isEnabled)
    val falseOption =
        SingleOptionDialogInput.OptionEntry.create("${key}_disabled", Component.text("Disabled"), !isEnabled)
    return DialogInput.singleOption(key, Component.text(label), listOf(trueOption, falseOption)).build()
}