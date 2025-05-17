package de.raphaelgoetz.buildLite.listener.minecraft

import de.raphaelgoetz.astralis.event.listenCancelled
import org.bukkit.event.raid.RaidFinishEvent
import org.bukkit.event.raid.RaidSpawnWaveEvent
import org.bukkit.event.raid.RaidStopEvent
import org.bukkit.event.raid.RaidTriggerEvent

fun registerRaidEvents() {
    listenCancelled<RaidTriggerEvent>()
    listenCancelled<RaidSpawnWaveEvent>()
    listenCancelled<RaidFinishEvent>()
    listenCancelled<RaidStopEvent>()
}