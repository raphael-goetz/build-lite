package de.raphaelgoetz.buildLite.record

const val WORLD_STATE_NAME_COLUMN_LENGTH = 20

enum class WorldState {
    NOT_STARTED,
    PLANING,
    UNDER_CONSTRUCTION,
    REVIEW_REQUIRED,
    FINISHED,
    ARCHIVED,
}