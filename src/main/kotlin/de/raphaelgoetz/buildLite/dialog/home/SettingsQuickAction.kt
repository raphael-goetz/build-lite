package de.raphaelgoetz.buildLite.dialog.home

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.keys.DialogKeys
import net.kyori.adventure.key.Key

internal val SETTINGS_DIALOG_KEY: TypedKey<Dialog> = DialogKeys.create(Key.key("build-lite:personal_preferences"))
internal val SETTINGS_BUILD_MODE_ACTION: Key = Key.key("build-lite:settings/build_mode")
internal val SETTINGS_NIGHT_MODE_ACTION: Key = Key.key("build-lite:settings/night_mode")
internal val SETTINGS_REVIEW_MODE_ACTION: Key = Key.key("build-lite:settings/review_mode")
internal val SETTINGS_SIDEBAR_ACTION: Key = Key.key("build-lite:settings/sidebar")
internal val SETTINGS_CLOSE_ACTION: Key = Key.key("build-lite:settings/close")
internal val SETTINGS_ACTIONS = setOf(
    SETTINGS_BUILD_MODE_ACTION,
    SETTINGS_NIGHT_MODE_ACTION,
    SETTINGS_REVIEW_MODE_ACTION,
    SETTINGS_SIDEBAR_ACTION,
    SETTINGS_CLOSE_ACTION,
)
