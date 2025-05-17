#!/usr/bin/env kotlin

import java.io.File
import java.util.Properties

// --- Configuration ---
val projectRoot = File(".")
val srcDir = File(projectRoot, "src")
val resourcesDir = File(projectRoot, "src/main/resources")
val propertiesFile = File(resourcesDir, "messages.properties")
val regex = Regex("""sendTransText\("([a-zA-Z0-9_.]+)"\)""")
val placeholderValue = "not implemented!"

// --- Load existing properties ---
val properties = Properties()
if (propertiesFile.exists()) {
    propertiesFile.inputStream().use { properties.load(it) }
} else {
    propertiesFile.parentFile.mkdirs()
    propertiesFile.createNewFile()
}

// --- Scan Kotlin source files ---
val foundKeys = mutableSetOf<String>()

srcDir.walkTopDown()
.filter { it.extension == "kt" }
.forEach { file ->
    val content = file.readText()
    regex.findAll(content).forEach { match ->
        val key = match.groupValues[1]
        foundKeys.add(key)
    }
}

// --- Add missing keys ---
var added = 0
foundKeys.forEach { key ->
    if (!properties.containsKey(key)) {
        properties.setProperty(key, placeholderValue)
        println("Added missing key: $key")
        added++
    }
}

// --- Save updated properties file ---
propertiesFile.outputStream().use { properties.store(it, "Synced by syncMessages.kts") }

println("✅ Done. $added key(s) added to messages.properties.")
