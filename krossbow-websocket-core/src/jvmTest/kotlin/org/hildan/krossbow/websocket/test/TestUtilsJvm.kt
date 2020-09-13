package org.hildan.krossbow.websocket.test

import com.pusher.java_websocket.WebSocket
import com.pusher.java_websocket.handshake.ClientHandshake
import com.pusher.java_websocket.server.WebSocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer

actual fun runSuspendingTest(block: suspend CoroutineScope.() -> Unit) = runBlocking { block() }

class EchoWebSocketServer(port: Int = 0) : WebSocketServer(InetSocketAddress(port)) {

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        conn?.send(message)
    }

    override fun onMessage(conn: WebSocket?, message: ByteBuffer) {
        val bytes = ByteArray(message.remaining())
        message.get(bytes)
        conn?.send(bytes)
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
    }

    fun startAndAwaitPort(): Int {
        start()
        return awaitPort()
    }

    private fun awaitPort(): Int = runBlocking {
        withTimeout(1000) {
            while (port <= 0) {
                delay(10)
            }
            port
        }
    }
}
