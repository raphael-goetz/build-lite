package de.raphaelgoetz.buildLite.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.bukkit.Bukkit
import java.io.File
import java.net.InetSocketAddress
import java.net.URLDecoder

class FileServer {

    private var server: HttpServer? = null

    fun start() {
        if (server != null) {
            throw IllegalStateException("Server is already running")
        }

        val baseDir = File(Bukkit.getPluginsFolder(), "BuildLite/export")
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }

        server = HttpServer.create(InetSocketAddress(8080), 0).apply {
            createContext("/file") { exchange ->
                val query = exchange.requestURI.query ?: ""
                val params = query.split("&")
                    .mapNotNull {
                        val parts = it.split("=")
                        if (parts.size == 2) parts[0] to parts[1] else null
                    }.toMap()

                val rawFilename = params["filename"]
                if (rawFilename == null) {
                    sendResponse(exchange, 400, "Missing 'filename' parameter")
                    return@createContext
                }

                val filename = URLDecoder.decode(rawFilename, "UTF-8")
                if (filename.contains("..") || filename.contains('/') || filename.contains('\\')) {
                    sendResponse(exchange, 400, "Invalid filename")
                    return@createContext
                }

                val file = File(baseDir, filename)
                if (!file.exists() || !file.isFile) {
                    sendResponse(exchange, 404, "File not found")
                    return@createContext
                }

                exchange.responseHeaders.add("Content-Type", "application/octet-stream")
                exchange.responseHeaders.add("Content-Disposition", "attachment; filename=\"$filename\"")
                exchange.sendResponseHeaders(200, file.length())

                file.inputStream().use { input ->
                    exchange.responseBody.use { output ->
                        input.copyTo(output)
                    }
                }
            }
            start()
        }
    }

    fun stop() {
        server?.stop(0)
        server = null // So that we can start again later
    }
}

fun sendResponse(exchange: HttpExchange, code: Int, message: String) {
    val bytes = message.toByteArray()
    exchange.sendResponseHeaders(code, bytes.size.toLong())
    exchange.responseBody.use { os ->
        os.write(bytes)
    }
}