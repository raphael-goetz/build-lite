package de.raphaelgoetz.buildLite.testsupport

import io.mockk.every
import io.mockk.mockk
import org.bukkit.entity.Player
import java.util.UUID

fun mockPlayer(uuid: UUID = UUID.randomUUID()): Player {
    val player = mockk<Player>()
    every { player.uniqueId } returns uuid
    return player
}
