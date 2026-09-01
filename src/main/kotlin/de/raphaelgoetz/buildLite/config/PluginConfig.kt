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
    val buildTimeAfkThresholdSeconds: Long,
    val buildTimeTickIntervalSeconds: Long,
    val r2AccountId: String,
    val r2AccessKeyId: String,
    val r2SecretAccessKey: String,
    val r2Bucket: String,
    val shareDefaultTtlMinutes: Int,
) {
    val r2Configured: Boolean
        get() = r2AccountId.isNotBlank() && r2AccessKeyId.isNotBlank() &&
            r2SecretAccessKey.isNotBlank() && r2Bucket.isNotBlank()
}