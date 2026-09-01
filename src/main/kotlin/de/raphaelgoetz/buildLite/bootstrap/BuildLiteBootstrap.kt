package de.raphaelgoetz.buildLite.bootstrap

import de.raphaelgoetz.buildLite.dialog.home.FIELD_FLY_SPEED_KEY
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_BUILD_MODE_ACTION
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_CLOSE_ACTION
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_DIALOG_KEY
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_NIGHT_MODE_ACTION
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_REVIEW_MODE_ACTION
import de.raphaelgoetz.buildLite.dialog.home.SETTINGS_SIDEBAR_ACTION
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.keys.tags.DialogTagKeys
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

class BuildLiteBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        context.lifecycleManager.registerEventHandler(RegistryEvents.DIALOG.compose()) { event ->
            event.registry().register(SETTINGS_DIALOG_KEY) { builder ->
                builder
                    .base(createSettingsBase())
                    .type(createSettingsType())
            }
        }

        context.lifecycleManager.registerEventHandler(
            LifecycleEvents.TAGS.postFlatten(RegistryKey.DIALOG)
        ) { event ->
            event.registrar().addToTag(DialogTagKeys.QUICK_ACTIONS, listOf(SETTINGS_DIALOG_KEY))
        }
    }
}

private fun createSettingsBase(): DialogBase {
    val speedInput = DialogInput.numberRange(
        FIELD_FLY_SPEED_KEY,
        200,
        Component.text("Fly Speed"),
        "%s: %s",
        0.0f,
        1.0f,
        0.1f,
        0.1f,
    )

    return DialogBase.builder(Component.text("Personal Preferences"))
        .externalTitle(Component.text("Personal Preferences"))
        .pause(false)
        .afterAction(DialogBase.DialogAfterAction.NONE)
        .inputs(listOf(speedInput))
        .build()
}

private fun createSettingsType() = DialogType.multiAction(
    listOf(
        createAction("Build Mode", "Enable or disable Build Mode.", SETTINGS_BUILD_MODE_ACTION),
        createAction("Night Vision", "Enable or disable Night Vision.", SETTINGS_NIGHT_MODE_ACTION),
        createAction("Review Visibility", "Show or hide review markers.", SETTINGS_REVIEW_MODE_ACTION),
        createAction("Build Sidebar", "Show or hide the build sidebar.", SETTINGS_SIDEBAR_ACTION),
    ),
    createAction("Close", "Close Personal Preferences.", SETTINGS_CLOSE_ACTION),
    2,
)

private fun createAction(label: String, tooltip: String, identifier: Key): ActionButton {
    return ActionButton.create(
        Component.text(label),
        Component.text(tooltip),
        150,
        DialogAction.customClick(identifier, null),
    )
}
