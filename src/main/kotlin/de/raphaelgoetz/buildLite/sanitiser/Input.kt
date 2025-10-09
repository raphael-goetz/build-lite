package de.raphaelgoetz.buildLite.sanitiser

fun String.sanitiseNameInput(): String {
    val san = replace(Regex("\\W"), "")

    if (san.length > 254) {
        return san.take(254)
    }

    return san.lowercase()
}

fun String.sanitiseGroupInput(): String {
    val san = replace(Regex("\\W"), "")

    if (san.length > 254) {
        return san.take(254)
    }

    return san.lowercase()
}


