package de.raphaelgoetz.buildLite.store

data class TeleportQueue(
    val player: BuildPlayer,
    val world: BuildWorld,
)