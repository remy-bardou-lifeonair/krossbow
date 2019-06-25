package org.hildan.krossbow.engines

import kotlin.reflect.KClass

/**
 * Creates an instance of [KrossbowClient] based on the given [KrossbowEngine]. The provided configuration function
 * is applied to the newly created client.
 */
@Suppress("FunctionName")
fun KrossbowClient(engine: KrossbowEngine, configure: KrossbowConfig.() -> Unit = {}): KrossbowClient {
    val config = KrossbowConfig().apply { configure() }
    return engine.createClient(config)
}

/**
 * An interface to define how to create a [KrossbowClient]. Implementations can build platform-specific clients.
 */
interface KrossbowEngine {

    /**
     * Creates a [KrossbowClient] based on the given config. Implementations SHOULD implement all supported
     * configuration, and MAY ignore configuration that are not supported by the underlying websocket STOMP client.
     */
    fun createClient(config: KrossbowConfig): KrossbowClient
}

/**
 * A STOMP client interface based on Websockets. The client is used to connect to the server and create a
 * [KrossbowSession]. Then, most of the STOMP interactions are done through the [KrossbowSession].
 */
interface KrossbowClient {

    /**
     * Connects to the given WebSocket [url] and to the STOMP session, and returns after receiving the CONNECTED frame.
     */
    suspend fun connect(url: String, login: String? = null, passcode: String? = null): KrossbowSession
}

/**
 * Connects to the given [url] and executes the given [block] with the created session. The session is then
 * automatically closed at the end of the block.
 */
suspend fun KrossbowClient.useSession(
    url: String,
    login: String? = null,
    passcode: String? = null,
    block: suspend KrossbowSession.() -> Unit
) {
    val session = connect(url, login, passcode)
    try {
        session.block()
    } finally {
        session.disconnect()
    }
}

/**
 * An adapter for Krossbow sessions in a platform-specific engine. Is can be used to easily create an actual
 * [KrossbowSession].
 */
interface KrossbowEngineSession {

    /**
     * Sends a SEND frame to the server at the given [destination] with the given [body].
     * A [KrossbowReceipt] is returned if receipts are activated and the server returns one.
     */
    suspend fun send(destination: String, body: Any): KrossbowReceipt?

    /**
     * Subscribes to the given [destination], expecting objects of type [clazz]. A platform-specific deserializer is
     * used to create instances of the given [clazz] from the body of every message received on the created
     * subscription. The subscription callbacks are adapted to the coroutines model by the actual [KrossbowSession].
     */
    suspend fun <T : Any> subscribe(
        destination: String,
        clazz: KClass<T>,
        callbacks: SubscriptionCallbacks<T>
    ): KrossbowEngineSubscription

    /**
     * Sends a DISCONNECT frame to close the session, and closes the connection.
     */
    suspend fun disconnect()
}

/**
 * An adapter for STOMP subscriptions in a platform-specific engine. Is can be used to easily create an actual
 * [KrossbowSubscription].
 */
data class KrossbowEngineSubscription(
    val id: String,
    val unsubscribe: suspend (UnsubscribeHeaders?) -> Unit
)
