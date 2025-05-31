package de.raphaelgoetz.buildLite.record

import de.raphaelgoetz.buildLite.store.BuildPlayer
import de.raphaelgoetz.buildLite.store.BuildServer
import de.raphaelgoetz.buildLite.store.BuildWorld
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.collections.map

object Warps : Table("warps") {
    val id = integer("id").autoIncrement()

    val worldUUID = uuid("world_uuid")
    val playerUUID = uuid("player_uuid")
    val isPrivate = bool("is_private")

    val name = varchar("name", 255)

    val x = double("x")
    val y = double("y")
    val z = double("z")
    val pitch = float("pitch")
    val yaw = float("yaw")
}

data class WarpRecord(val id: Int, val worldUUID: UUID, val playerUUID: UUID, val name: String, val x: Double, val y: Double, val z: Double, val pitch: Float, val yaw: Float)

fun getGlobalWarps(): List<WarpRecord> {
    return transaction {
       val result = Warps
            .selectAll()
            .where {( Warps.isPrivate eq false) }
            .map {
                WarpRecord(
                    id = it[Warps.id],
                    worldUUID = it[Warps.worldUUID],
                    playerUUID = it[Warps.playerUUID],
                    name = it[Warps.name],
                    x = it[Warps.x],
                    y = it[Warps.y],
                    z = it[Warps.z],
                    pitch = it[Warps.pitch],
                    yaw = it[Warps.yaw]
                )
            }

        Warps
            .selectAll()
            .map {
                WarpRecord(
                    id = it[Warps.id],
                    worldUUID = it[Warps.worldUUID],
                    playerUUID = it[Warps.playerUUID],
                    name = it[Warps.name],
                    x = it[Warps.x],
                    y = it[Warps.y],
                    z = it[Warps.z],
                    pitch = it[Warps.pitch],
                    yaw = it[Warps.yaw]
                )
            }.forEach { println(it.toString()) }

     return@transaction result
    }
}

fun BuildPlayer.getPrivateWarps(): List<WarpRecord> {
    return transaction {
        Warps
            .selectAll()
            .where {( Warps.isPrivate eq true) and (Warps.playerUUID eq player.uniqueId) }
            .map {
                WarpRecord(
                    worldUUID = it[Warps.worldUUID],
                    playerUUID = it[Warps.playerUUID],
                    name = it[Warps.name],
                    x = it[Warps.x],
                    y = it[Warps.y],
                    z = it[Warps.z],
                    pitch = it[Warps.pitch],
                    yaw = it[Warps.yaw],
                    id = it[Warps.id]
                )
            }
    }
}

fun createWarp(world: BuildWorld, player: BuildPlayer, name: String, isPrivate: Boolean) {
    val location = player.player.location
    transaction {
        Warps.insert {
            it[Warps.worldUUID] = world.meta.id
            it[Warps.playerUUID] = player.player.uniqueId
            it[Warps.isPrivate] = isPrivate
            it[Warps.name] = name

            it[Warps.x] = location.x
            it[Warps.y] = location.y
            it[Warps.z] = location.z
            it[Warps.pitch] = location.pitch
            it[Warps.yaw] = location.yaw
        }
    }
}

fun BuildPlayer.createPublicWarp(server: BuildServer, name: String) {
    val world = server.asBuildWorld(player.world.name) ?: return
    createWarp(world, this, name, false)
}


fun BuildPlayer.createPrivateWarp(server: BuildServer, name: String) {
    val world = server.asBuildWorld(player.world.name) ?: return
    createWarp(world, this, name, true)
}

fun getPublicWarp(name: String): WarpRecord? {
    return transaction {
        Warps
            .selectAll()
            .where {
                (Warps.name eq name) and
                (Warps.isPrivate eq false)
            }
            .map {
                WarpRecord(
                    worldUUID = it[Warps.worldUUID],
                    playerUUID = it[Warps.playerUUID],
                    name = it[Warps.name],
                    x = it[Warps.x],
                    y = it[Warps.y],
                    z = it[Warps.z],
                    pitch = it[Warps.pitch],
                    yaw = it[Warps.yaw],
                    id = it[Warps.id]
                )
            }.firstOrNull()
    }
}

fun getPrivateWarp(playerUUID: UUID, name: String): WarpRecord? {
    return transaction {
        Warps
            .selectAll()
            .where {
                (Warps.playerUUID eq playerUUID) and
                (Warps.name eq name) and
                (Warps.isPrivate eq true)
            }
            .map {
                WarpRecord(
                    worldUUID = it[Warps.worldUUID],
                    playerUUID = it[Warps.playerUUID],
                    name = it[Warps.name],
                    x = it[Warps.x],
                    y = it[Warps.y],
                    z = it[Warps.z],
                    pitch = it[Warps.pitch],
                    yaw = it[Warps.yaw],
                    id = it[Warps.id]
                )
            }.firstOrNull()
    }
}


fun deleteWarp(id: Int) {
    transaction {
        Warps.deleteWhere { Warps.id eq id }
    }
}

fun BuildWorld.deleteAllWarps() {
    transaction {
        Warps.deleteWhere {
            Warps.worldUUID eq this@deleteAllWarps.meta.id
        }
    }
}