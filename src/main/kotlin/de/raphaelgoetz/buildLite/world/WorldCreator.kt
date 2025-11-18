package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.astralis.event.unregister
import de.raphaelgoetz.astralis.schedule.doLater
import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.generator.ChunkGenerator

object WorldCreator {

    fun create(
        name: String,
        generator: ChunkGenerator,
    ) {
        val worldGenerator = WorldCreator(name)
        worldGenerator.environment(World.Environment.NORMAL)
        worldGenerator.generator(generator)

        // ALERT! This needs to be called before worldGenerator#createWorld()
        val listener = listen<WorldLoadEvent> { event ->
            if (event.world.name != name) return@listen
            val world = event.world
            world.difficulty = Difficulty.PEACEFUL

            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
            world.setGameRule(GameRule.SPAWN_RADIUS, 0)

            world.setGameRule(GameRule.DO_FIRE_TICK, false)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.setGameRule(GameRule.MOB_GRIEFING, false)
            world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false)
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            world.setGameRule(GameRule.DO_ENTITY_DROPS, false)
            world.setGameRule(GameRule.DO_INSOMNIA, false)
            world.setGameRule(GameRule.FALL_DAMAGE, false)
            world.setGameRule(GameRule.FIRE_DAMAGE, false)
            world.setGameRule(GameRule.FREEZE_DAMAGE, false)

            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
            world.setGameRule(GameRule.DISABLE_RAIDS, true)
            world.setGameRule(GameRule.KEEP_INVENTORY, true)
        }

        worldGenerator.createWorld()

        doLater(5) {
            listener.unregister()
        }
    }
}