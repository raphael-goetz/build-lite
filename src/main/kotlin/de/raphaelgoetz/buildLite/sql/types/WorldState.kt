package de.raphaelgoetz.buildLite.sql.types

const val WORLD_STATE_NAME_COLUMN_LENGTH = 20

enum class WorldState(val text: String) {
    NOT_STARTED("Not Started"),
    PLANING("Planning"),
    UNDER_CONSTRUCTION("Under Construction"),
    REVIEW_REQUIRED("Review Required"),
    FINISHED("Finished"),
    ARCHIVED("Archived"),
}

fun WorldState.asDialogInput(): String = this.toString()

fun String.toWorldState(): WorldState = WorldState.valueOf(this)