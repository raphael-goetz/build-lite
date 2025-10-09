package de.raphaelgoetz.buildLite.config

import de.raphaelgoetz.buildLite.sql.RecordWorld
import de.raphaelgoetz.buildLite.sql.getSqlPlayerCredits
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

data class WorldMeta(
    val uniqueId: String,
    val creatorUniqueId: String,
    val name: String,
    val group: String,
    val state: String,
    val generator: String,
    val location: MetaLocation,
    val creditUniqueIds: List<String>
)

data class MetaLocation(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
)

fun RecordWorld.toMeta(): WorldMeta {

    val credits = getSqlPlayerCredits().map { it.playerUuid.toString() }
    val location = MetaLocation(
        loadableSpawn.x,
        loadableSpawn.y,
        loadableSpawn.z,
        loadableSpawn.yaw,
        loadableSpawn.pitch,
    )
    return WorldMeta(
        uniqueId.toString(), creatorUuid.toString(), name, group, state.text, generator.text, location, credits
    )
}


fun Any.toJson(indent: String = ""): String {
    return when (this) {
        is String -> "\"${this.replace("\"", "\\\"")}\""
        is Number, is Boolean -> this.toString()
        is List<*> -> this.joinToString(prefix = "[", postfix = "]") { it?.toJson("$indent  ") ?: "null" }
        else -> {
            val props = this::class.memberProperties.joinToString(",\n") { prop ->
                @Suppress("UNCHECKED_CAST")
                val value = (prop as KProperty1<Any, *>).get(this)
                "$indent  \"${prop.name}\": ${value?.toJson("$indent  ") ?: "null"}"
            }
            "{\n$props\n$indent}"
        }
    }
}
