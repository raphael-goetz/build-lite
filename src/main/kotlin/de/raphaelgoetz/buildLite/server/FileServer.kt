package de.raphaelgoetz.buildLite.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import de.raphaelgoetz.buildLite.config.PluginConfig
import org.bukkit.Bukkit
import java.io.File
import java.net.InetSocketAddress

class FileServer(val config: PluginConfig) {

    private var server: HttpServer? = null

    fun start() {
        if (server != null) throw IllegalStateException("Server is already running")

        val baseDir = File(Bukkit.getPluginsFolder(), "build-lite/export").apply { mkdirs() }
        config
        server = HttpServer.create(InetSocketAddress(config.host, config.port), 0).apply {
            createContext("/uuid") { exchange ->
                try {
                    val path = exchange.requestURI.path.removePrefix("/uuid/").trim()
                    if (path.isEmpty()) {
                        sendResponse(exchange, 400, "Missing UUID")
                        return@createContext
                    }

                    val uuidRegex = Regex("^[0-9a-fA-F\\-]{36}$")
                    if (!uuidRegex.matches(path)) {
                        sendResponse(exchange, 400, "Invalid UUID format")
                        return@createContext
                    }

                    val file = File(baseDir, "$path.tar.gz")
                    if (!file.exists() || !file.isFile) {
                        sendResponse(exchange, 404, "File not found")
                        return@createContext
                    }

                    exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
                    exchange.responseHeaders.add("Content-Type", "application/gzip")
                    exchange.responseHeaders.add(
                        "Content-Disposition",
                        "attachment; filename=\"$path.tar.gz\""
                    )

                    exchange.sendResponseHeaders(200, 0) // chunked transfer

                    file.inputStream().use { input ->
                        exchange.responseBody.use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    Bukkit.getLogger().warning("Error handling file request: ${e.message}")
                    sendResponse(exchange, 500, "Internal server error")
                } finally {
                    exchange.close()
                }
            }
            start()
        }

        Bukkit.getLogger().info("File server started at http://${config.host}:${config.port}/uuid/<uuid>")
    }

    fun stop() {
        server?.stop(0)
        server = null
        Bukkit.getLogger().info("File server stopped.")
    }
}

fun sendResponse(exchange: HttpExchange, code: Int, message: String) {
    val bytes = message.toByteArray()
    exchange.sendResponseHeaders(code, bytes.size.toLong())
    exchange.responseBody.use { it.write(bytes) }
}
