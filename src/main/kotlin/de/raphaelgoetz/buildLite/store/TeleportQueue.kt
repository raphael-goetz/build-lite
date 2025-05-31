package de.raphaelgoetz.buildLite.store

import de.raphaelgoetz.buildLite.record.WarpRecord

data class TeleportQueue(
    val player: BuildPlayer,
    val world: BuildWorld,
    val warp: WarpRecord?
)