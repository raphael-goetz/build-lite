package de.raphaelgoetz.buildLite.listener.minecraft

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.astralis.event.listenCancelled
import de.raphaelgoetz.buildLite.listener.cancelWhenBuilder
import de.raphaelgoetz.buildLite.registry.Door
import org.bukkit.Material
import org.bukkit.event.EventPriority
import org.bukkit.event.block.*

fun registerBlockEvents() {

    listen<BlockBreakEvent>(EventPriority.LOWEST) { blockBreakEvent ->
        val player = blockBreakEvent.player

        if (player.activeItem.type == Material.WOODEN_AXE) {
            blockBreakEvent.isCancelled = true
            return@listen
        }

        player.cancelWhenBuilder(blockBreakEvent)
    }

    listen<BlockPhysicsEvent> { blockPhysicsEvent ->
        val material = blockPhysicsEvent.changedBlockData.material

        if (material.isDoor()) return@listen
        blockPhysicsEvent.isCancelled = true
    }

    listen<BlockPlaceEvent> { blockPlaceEvent ->
        val player = blockPlaceEvent.player
        player.cancelWhenBuilder(blockPlaceEvent)
    }

    listenCancelled<BlockBurnEvent>()
    listenCancelled<BlockDispenseArmorEvent>()
    listenCancelled<BlockDispenseEvent>()
    listenCancelled<BlockDropItemEvent>()
    listenCancelled<BlockExplodeEvent>()
    listenCancelled<BlockFadeEvent>()
    listenCancelled<BlockGrowEvent>()
    listenCancelled<BlockIgniteEvent>()
    listenCancelled<BlockPistonExtendEvent>()
    listenCancelled<BlockPistonRetractEvent>()
    listenCancelled<BlockReceiveGameEvent>()
    listenCancelled<BlockSpreadEvent>()
    listenCancelled<FluidLevelChangeEvent>()
    listenCancelled<LeavesDecayEvent>()
}

private fun Material.isDoor(): Boolean {

    for (door in Door.entries) {
        if (door.material != this) continue
        return true
    }

    return false
}

