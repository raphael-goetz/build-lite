package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.buildLite.sql.RecordWorld

data class WorldFolder(
    val group: String,
    val worlds: List<RecordWorld>
)