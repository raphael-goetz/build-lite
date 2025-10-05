package de.raphaelgoetz.buildLite.listener.minecraft

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.astralis.event.listenCancelled
import de.raphaelgoetz.buildLite.registry.Door
import org.bukkit.Material
import org.bukkit.event.block.*

//fun registerBlockEvents(server: BuildServer) {
//
//    listen<BlockBreakEvent> { blockBreakEvent ->
//        val player = server.asBuildPlayer(blockBreakEvent.player) ?: return@listen
//
//        if (!player.isBuilding) blockBreakEvent.isCancelled = true
//        if (player.player.itemInHand.type == Material.WOODEN_AXE) blockBreakEvent.isCancelled = true
//    }
//
//    listen<BlockPhysicsEvent> { blockPhysicsEvent ->
//        val material = blockPhysicsEvent.changedBlockData.material
//
//        if (material.isDoor()) return@listen
//        val world = server.asBuildWorld(blockPhysicsEvent.block.world) ?: return@listen
//        val value = world.hasPhysics
//        blockPhysicsEvent.isCancelled = !value
//    }
//
//    listen<BlockPlaceEvent> { blockPlaceEvent ->
//        val player = server.asBuildPlayer(blockPlaceEvent.player) ?: return@listen
//        player.cancelWhenBuilder(blockPlaceEvent)
//    }
//
//    listenCancelled<BlockBurnEvent>()
//    listenCancelled<BlockDispenseArmorEvent>()
//    listenCancelled<BlockDispenseEvent>()
//    listenCancelled<BlockDropItemEvent>()
//    listenCancelled<BlockExplodeEvent>()
//    listenCancelled<BlockFadeEvent>()
//    listenCancelled<BlockGrowEvent>()
//    listenCancelled<BlockIgniteEvent>()
//    listenCancelled<BlockPistonExtendEvent>()
//    listenCancelled<BlockPistonRetractEvent>()
//    listenCancelled<BlockReceiveGameEvent>()
//    listenCancelled<BlockSpreadEvent>()
//    listenCancelled<FluidLevelChangeEvent>()
//    listenCancelled<LeavesDecayEvent>()
//}
//
//private fun Material.isDoor(): Boolean {
//
//    for (door in Door.entries) {
//        if (door.material != this) continue
//        return true
//    }
//
//    return false
//}
//
//