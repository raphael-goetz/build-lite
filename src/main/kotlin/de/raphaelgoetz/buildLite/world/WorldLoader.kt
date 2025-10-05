package de.raphaelgoetz.buildLite.world

import de.raphaelgoetz.astralis.event.listen
import de.raphaelgoetz.astralis.event.unregister
import de.raphaelgoetz.astralis.schedule.doLater
import de.raphaelgoetz.buildLite.sql.types.WorldGenerator
import de.raphaelgoetz.buildLite.sql.types.toGenerator
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.event.world.WorldLoadEvent

object WorldLoader {

    fun lazyLoad(loadableWorld: LoadableWorld, generator: WorldGenerator) {
        if (Bukkit.getWorld(loadableWorld.uniqueId.toString()) != null) return
        val creator = WorldCreator(loadableWorld.uniqueId.toString())
        creator.generator(generator.toGenerator())
        creator.createWorld()
    }

    fun lazyTeleport(loadableLocation: LoadableLocation, generator: WorldGenerator, player: Player) {
        val world = Bukkit.getWorld(loadableLocation.worldUuid.toString())
        if (world != null) {
            val location = loadableLocation.toLocation(world)
            player.teleportAsync(location)
            return
        }

        val listener = listen<WorldLoadEvent> { event ->
            if (event.world.name != loadableLocation.worldUuid.toString()) {
                return@listen
            }

            val world = event.world
            val location = loadableLocation.toLocation(world)
            player.teleportAsync(location)
        }

        lazyLoad(LoadableWorld(loadableLocation.worldUuid), generator)

        doLater(2000) {
            listener.unregister()
        }

    }

    fun lazyUnload(world: World) {
        Bukkit.unloadWorld(world, true)
    }

    fun lazyDelete() {
        //TODO: Delete World Favorite, Credit & Players Last Locations aslo the world record and the world folder
        //Lazy unload before with players
    }
}