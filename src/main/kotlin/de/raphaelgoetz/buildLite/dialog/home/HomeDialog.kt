package de.raphaelgoetz.buildLite.dialog.home

import de.raphaelgoetz.buildLite.action.actionDisableBuildMode
import de.raphaelgoetz.buildLite.action.actionDisableNightMode
import de.raphaelgoetz.buildLite.action.actionEnableBuildMode
import de.raphaelgoetz.buildLite.action.actionEnableNightMode
import de.raphaelgoetz.buildLite.cache.PlayerCache
import de.raphaelgoetz.buildLite.dialog.warp.showWarpCreationDialog
import de.raphaelgoetz.buildLite.dialog.world.showWorldCreationDialog
import de.raphaelgoetz.buildLite.menu.openBannerCreationMenu
import de.raphaelgoetz.buildLite.menu.openPlayerMenu
import de.raphaelgoetz.buildLite.menu.openWarpMenu
import de.raphaelgoetz.buildLite.menu.openWorldFolderMenu
import de.raphaelgoetz.buildLite.menu.openWorldMigrationMenu
import de.raphaelgoetz.buildLite.world.WorldMigrator
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

private const val FIELD_BUILD_MODE_KEY = "home_build_mode"
private const val FIELD_NIGHT_MODE_KEY = "home_night_mode"
private const val FIELD_SPEED_KEY = "home_speed"

fun Player.showHomeDialog() {
    showDialog(createHomeDialog())
}

private fun Player.createHomeDialog(): Dialog {
    val cachedPlayer = PlayerCache.getOrInit(this)

    //Inputs
    val buildModeInput = createToggleButton(FIELD_BUILD_MODE_KEY, "Build", cachedPlayer.recordPlayer.buildMode)
    val nightModeInput = createToggleButton(FIELD_NIGHT_MODE_KEY, "Night Vision", cachedPlayer.recordPlayer.nightMode)
    val flySpeedInput = DialogInput.numberRange(
        FIELD_SPEED_KEY, 200, Component.text("Speed"), "Fly Speed", 0.1f, 1f, this.flySpeed, 0.1f
    )

    //Actions
    val actions = mutableListOf(
        createAction("World Menu", "Click to open the World Menu") { _, _ ->
            openWorldFolderMenu()
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
        createAction("Banner Menu", "Click to open the Banner Menu") { _, _ ->
            openBannerCreationMenu()
        },
    )

    if (WorldMigrator.detect().isNotEmpty()) {
        actions.add(createAction("Migrate Worlds", "Click to open the Migration Menu", 200) { _, _ ->
            openWorldMigrationMenu()
        })
    }

    val closeAction = createAction("Close", "This will open the world Menu") { view, _ ->
        val speed = view.getFloat(FIELD_SPEED_KEY)
        val nightMode = view.getText(FIELD_NIGHT_MODE_KEY)
        val buildMode= view.getText(FIELD_BUILD_MODE_KEY)

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
    }

    val base = DialogBase.builder(Component.text("Home Menu"))
        .inputs(listOf(buildModeInput, nightModeInput, flySpeedInput)).build()
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

private fun Player.createAction(
    title: String, toolTip: String, width: Int = 100, consumer: (DialogResponseView, Audience) -> Unit
): ActionButton {
    return ActionButton.create(
        Component.text(title), Component.text(toolTip), width, DialogAction.customClick(
            consumer, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        )
    )
}
