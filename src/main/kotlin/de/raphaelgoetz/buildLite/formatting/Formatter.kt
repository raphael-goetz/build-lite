package de.raphaelgoetz.buildLite.formatting

fun String.capitalizeFirst(): String = this.lowercase().replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}
