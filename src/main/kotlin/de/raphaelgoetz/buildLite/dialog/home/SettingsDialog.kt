package de.raphaelgoetz.buildLite.dialog.home

import de.raphaelgoetz.buildLite.action.actionDisableBuildMode
import de.raphaelgoetz.buildLite.action.actionDisableNightMode
import de.raphaelgoetz.buildLite.action.actionDisableReviewMode
import de.raphaelgoetz.buildLite.action.actionEnableBuildMode
import de.raphaelgoetz.buildLite.action.actionEnableNightMode
import de.raphaelgoetz.buildLite.action.actionEnableReviewMode
import de.raphaelgoetz.buildLite.cache.PlayerCache
import de.raphaelgoetz.buildLite.dialog.createAction
import de.raphaelgoetz.buildLite.scoreboard.BuildScoreboard
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

internal const val FIELD_FLY_SPEED_KEY = "settings_fly_speed"

fun Player.showSettingsDialog(returnToHome: Boolean = false) {
    showDialog(createSettingsDialog(returnToHome))
}

private fun Player.createSettingsDialog(returnToHome: Boolean): Dialog {
    val record = PlayerCache.getOrInit(this).recordPlayer
    val speedInput = DialogInput.numberRange(
        FIELD_FLY_SPEED_KEY,
        200,
        Component.text("Fly Speed"),
        "%s: %s",
        0.0f,
        1.0f,
        flySpeed,
        0.1f,
    )
    val actions = listOf(
        createPreferenceAction("Build Mode", record.buildMode, returnToHome) {
            toggleBuildModePreference()
        },
        createPreferenceAction("Night Vision", record.nightMode, returnToHome) {
            toggleNightModePreference()
        },
        createPreferenceAction("Review Visibility", record.reviewMode, returnToHome) {
            toggleReviewModePreference()
        },
        createPreferenceAction("Build Sidebar", BuildScoreboard.isEnabled(this), returnToHome) {
            toggleBuildSidebarPreference()
        },
    )

    val exitLabel = if (returnToHome) "Back" else "Close"
    val exitTooltip = if (returnToHome) {
        "Apply fly speed and return to the main menu."
    } else {
        "Apply fly speed and close settings."
    }
    val exit = createAction(exitLabel, exitTooltip) { view, _ ->
        applyFlySpeed(view.getFloat(FIELD_FLY_SPEED_KEY))
        if (returnToHome) showHomeDialog(false) else closeDialog()
    }
    val base = DialogBase.builder(Component.text("Personal Preferences"))
        .pause(false)
        .afterAction(DialogBase.DialogAfterAction.NONE)
        .inputs(listOf(speedInput))
        .build()
    val type = DialogType.multiAction(actions, exit, 2)

    return Dialog.create { factory ->
        factory.empty().base(base).type(type)
    }
}

private fun Player.createPreferenceAction(
    label: String,
    enabled: Boolean,
    returnToHome: Boolean,
    toggle: Player.() -> Unit,
): ActionButton {
    val state = if (enabled) "ON" else "OFF"
    return createAction("$label: $state", "Click to turn $label ${if (enabled) "off" else "on"}.") { view, _ ->
        applyFlySpeed(view.getFloat(FIELD_FLY_SPEED_KEY))
        toggle()
        showSettingsDialog(returnToHome)
    }
}

private fun Player.applyFlySpeed(speed: Float?) {
    speed?.let { flySpeed = it }
}

internal fun Player.applySettingsFlySpeed(speed: Float?) {
    applyFlySpeed(speed)
}

internal fun Player.toggleBuildModePreference() {
    val record = PlayerCache.getOrInit(this).recordPlayer
    if (record.buildMode) actionDisableBuildMode() else actionEnableBuildMode()
}

internal fun Player.toggleNightModePreference() {
    val record = PlayerCache.getOrInit(this).recordPlayer
    if (record.nightMode) actionDisableNightMode() else actionEnableNightMode()
}

internal fun Player.toggleReviewModePreference() {
    val record = PlayerCache.getOrInit(this).recordPlayer
    if (record.reviewMode) actionDisableReviewMode() else actionEnableReviewMode()
}

internal fun Player.toggleBuildSidebarPreference() {
    BuildScoreboard.toggle(this)
}
