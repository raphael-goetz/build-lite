package de.raphaelgoetz.buildLite.config

data class PluginConfig(
    val dbDriver: String,
    val dbUrl: String,
    val hasServer: Boolean,
    val host: String,
    val port: Int,
    val spawnX: Double,
    val spawnY: Double,
    val spawnZ: Double,
    val spawnPitch: Float,
    val spawnYaw: Float,
)