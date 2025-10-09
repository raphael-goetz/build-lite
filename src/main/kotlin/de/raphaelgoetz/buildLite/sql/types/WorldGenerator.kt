package de.raphaelgoetz.buildLite.sql.types

import de.raphaelgoetz.astralis.world.generator.ChessGenerator
import de.raphaelgoetz.astralis.world.generator.VoidGenerator
import org.bukkit.generator.ChunkGenerator

const val WORLD_GENERATOR_NAME_COLUMN_LENGTH = 20

// Enum describing generator type
enum class WorldGenerator(val text: String) {
    VOID("Void"),
    CHESS("Chessboard"),
}

// Extension function
fun WorldGenerator.toGenerator(): ChunkGenerator = when (this) {
    WorldGenerator.VOID -> VoidGenerator()
    WorldGenerator.CHESS -> ChessGenerator()
}

fun WorldGenerator.asDialogInput(): String = this.toString()

fun String.toWorldGenerator(): WorldGenerator = WorldGenerator.valueOf(this)
