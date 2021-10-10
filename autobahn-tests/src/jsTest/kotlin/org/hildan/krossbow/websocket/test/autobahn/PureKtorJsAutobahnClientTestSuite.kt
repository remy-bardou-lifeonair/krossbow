package org.hildan.krossbow.websocket.test.autobahn

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.hildan.krossbow.websocket.*
import org.hildan.krossbow.websocket.test.*
import kotlin.test.*

class PureKtorJsAutobahnClientTestSuite {
    private val agentUnderTest: String = "pure-ktor-js-${environment()}"
    private val testServerUrl: String = getDefaultAutobahnConfig().websocketTestServerUrl

    @Test
    fun autobahn_1_1_1_echo_text_payload() = runAutobahnTestCase("1.1.1")

    @Test
    fun autobahn_1_1_2_echo_text_payload() = runAutobahnTestCase("1.1.2")

    @Test
    fun autobahn_1_1_3_echo_text_payload() = runAutobahnTestCase("1.1.3")

    @Test
    fun autobahn_1_1_4_echo_text_payload() = runAutobahnTestCase("1.1.4")

    @Test
    fun autobahn_1_1_5_echo_text_payload() = runAutobahnTestCase("1.1.5")

    @Test
    fun autobahn_1_1_6_echo_text_payload() = runAutobahnTestCase("1.1.6")

    @Test
    fun autobahn_1_1_7_echo_text_payload() = runAutobahnTestCase("1.1.7")

    @Test
    fun autobahn_1_1_8_echo_text_payload() = runAutobahnTestCase("1.1.8")

    @Test
    fun autobahn_1_2_1_echo_binary_payload() = runAutobahnTestCase("1.2.1")

    @Test
    fun autobahn_1_2_2_echo_binary_payload() = runAutobahnTestCase("1.2.2")

    @Test
    fun autobahn_1_2_3_echo_binary_payload() = runAutobahnTestCase("1.2.3")

    @Test
    fun autobahn_1_2_4_echo_binary_payload() = runAutobahnTestCase("1.2.4")

    @Test
    fun autobahn_1_2_5_echo_binary_payload() = runAutobahnTestCase("1.2.5")

    @Test
    fun autobahn_1_2_6_echo_binary_payload() = runAutobahnTestCase("1.2.6")

    @Test
    fun autobahn_1_2_7_echo_binary_payload() = runAutobahnTestCase("1.2.7")

    @Test
    fun autobahn_1_2_8_echo_binary_payload() = runAutobahnTestCase("1.2.8")

    @Test
    fun autobahn_2_1_ping_pong() = runAutobahnTestCase("2.1")

    @Test
    fun autobahn_2_2_ping_pong() = runAutobahnTestCase("2.2")

    @Test
    fun autobahn_2_3_ping_pong() = runAutobahnTestCase("2.3")

    @Test
    fun autobahn_2_4_ping_pong() = runAutobahnTestCase("2.4")

    @Test
    fun autobahn_2_5_ping_pong() = runAutobahnTestCase("2.5")

    @Test
    fun autobahn_2_6_ping_pong() = runAutobahnTestCase("2.6")

    @Test
    fun autobahn_2_7_ping_pong() = runAutobahnTestCase("2.7")

    @Test
    fun autobahn_2_8_ping_pong() = runAutobahnTestCase("2.8")

    @Test
    fun autobahn_2_9_ping_pong() = runAutobahnTestCase("2.9")

    @Ignore // FIXME not all pongs are necessary according to the spec, so FAILED status is acceptable here
    @Test
    fun autobahn_2_10_ping_pong() = runAutobahnTestCase("2.10")

    @Ignore // FIXME not all pongs are necessary according to the spec, so FAILED status is acceptable here
    @Test
    fun autobahn_2_11_ping_pong() = runAutobahnTestCase("2.11")

    @Test
    fun autobahn_3_1_reserved_bits() = runAutobahnTestCase("3.1")

    @Test
    fun autobahn_3_2_reserved_bits() = runAutobahnTestCase("3.2")

    @Test
    fun autobahn_3_3_reserved_bits() = runAutobahnTestCase("3.3")

    @Test
    fun autobahn_3_4_reserved_bits() = runAutobahnTestCase("3.4")

    @Test
    fun autobahn_3_5_reserved_bits() = runAutobahnTestCase("3.5")

    @Test
    fun autobahn_3_6_reserved_bits() = runAutobahnTestCase("3.6")

    @Test
    fun autobahn_3_7_reserved_bits() = runAutobahnTestCase("3.7")

    @Test
    fun autobahn_4_1_1_opcodes() = runAutobahnTestCase("4.1.1")

    @Test
    fun autobahn_4_1_2_opcodes() = runAutobahnTestCase("4.1.2")

    @Test
    fun autobahn_4_1_3_opcodes() = runAutobahnTestCase("4.1.3")

    @Test
    fun autobahn_4_1_4_opcodes() = runAutobahnTestCase("4.1.4")

    @Test
    fun autobahn_4_1_5_opcodes() = runAutobahnTestCase("4.1.5")

    @Test
    fun autobahn_4_2_1_opcodes() = runAutobahnTestCase("4.2.1")

    @Test
    fun autobahn_4_2_2_opcodes() = runAutobahnTestCase("4.2.2")

    @Test
    fun autobahn_4_2_3_opcodes() = runAutobahnTestCase("4.2.3")

    @Test
    fun autobahn_4_2_4_opcodes() = runAutobahnTestCase("4.2.4")

    @Test
    fun autobahn_4_2_5_opcodes() = runAutobahnTestCase("4.2.5")

    @Test
    fun autobahn_5_1_echo_payload() = runAutobahnTestCase("5.1")

    @Test
    fun autobahn_5_2_echo_payload() = runAutobahnTestCase("5.2")

    @Test
    fun autobahn_5_3_echo_payload() = runAutobahnTestCase("5.3")

    @Test
    fun autobahn_5_4_echo_payload() = runAutobahnTestCase("5.4")

    @Test
    fun autobahn_5_5_echo_payload() = runAutobahnTestCase("5.5")

    @Test
    fun autobahn_5_6_echo_payload() = runAutobahnTestCase("5.6")

    @Test
    fun autobahn_5_7_echo_payload() = runAutobahnTestCase("5.7")

    @Test
    fun autobahn_5_8_echo_payload() = runAutobahnTestCase("5.8")

    @Test
    fun autobahn_5_9_echo_payload() = runAutobahnTestCase("5.9")

    private fun runAutobahnTestCase(caseId: String) = runSuspendingTest {
        val ktorHttp = HttpClient { install(WebSockets) }
        val autobahnClientTester = KtorAutobahnClientTester(ktorHttp, testServerUrl, agentUnderTest)
        try {
            autobahnClientTester.runTestCase(AutobahnCase.fromTuple(caseId))
        } catch (t: Throwable) { // we need to also catch AssertionError
//            println("Test case $caseId failed for agent $agentUnderTest, writing autobahn reports...")
//            // It would be best to do that only after all tests of the class, but it's not possible at the moment.
//            // In the meantime, we only write reports in case of error because updateReports itself fails sometimes.
//            // We want to keep the original exception so we rethrow it, but we also add exceptions from updateReports
//            // as suppressed exceptions in case it fails as well.
//            val reportResult = runCatching { autobahnClientTester.updateReports() }
//            reportResult.exceptionOrNull()?.let { t.addSuppressed(it) }
            throw t
        } finally {
            autobahnClientTester.updateReports()
        }
    }

    private suspend fun KtorAutobahnClientTester.runTestCase(case: AutobahnCase) {
        try {
            withTimeout(10000) {
                val session = connectForAutobahnTestCase(case.id)
                session.echoUntilClosed()
            }
            val status = getCaseStatus(case.id)
            val testResultAcceptable = status == TestCaseStatus.OK || status == TestCaseStatus.NON_STRICT
            assertTrue(testResultAcceptable,"Test case ${case.id} finished with status ${status}, expected OK or NON-STRICT")
        } catch (e: TimeoutCancellationException) {
            fail("Test case ${case.id} timed out", e)
        } catch (e: Exception) {
            if (!case.expectFailure) {
                println("Unexpected exception during test case ${case.id}:\n${e.stackTraceToString()}")
                throw IllegalStateException("Unexpected exception during test case ${case.id}", e)
            }
        }
    }
}

private suspend fun DefaultClientWebSocketSession.echoUntilClosed() {
    incoming.receiveAsFlow().takeWhile { it !is Frame.Close }.collect {
        echoFrame(it)
    }
}

private suspend fun DefaultClientWebSocketSession.echoFrame(frame: Frame) {
    when (frame) {
        is Frame.Text -> send(frame.data.decodeToString())
        is Frame.Binary -> send(frame.data)
        is Frame.Ping,
        is Frame.Pong -> Unit
        is Frame.Close -> error("should not receive CLOSE frame at that point")
    }
}

private class KtorAutobahnClientTester(
    private val wsClient: HttpClient,
    private val testServerUrl: String,
    private val agentUnderTest: String,
) {
    suspend fun connectForAutobahnTestCase(case: String): DefaultClientWebSocketSession {
        return wsClient.connect("$testServerUrl/runCase?casetuple=$case&agent=$agentUnderTest")
    }

    suspend fun getCaseCount(): Int = callAndGetJson("getCaseCount")

    suspend fun getCaseInfo(case: String): AutobahnCaseInfo = callAndGetJson("getCaseInfo?casetuple=$case")

    suspend fun getCaseStatus(case: String): TestCaseStatus =
        callAndGetJson<AutobahnCaseStatus>("getCaseStatus?casetuple=$case&agent=$agentUnderTest").behavior

    suspend fun updateReports(): Unit = call("updateReports?agent=$agentUnderTest")

    suspend fun stopServer(): Unit = call("stopServer")

    @OptIn(ExperimentalSerializationApi::class)
    private suspend inline fun <reified T> callAndGetJson(endpoint: String): T {
        val frameText = wsClient.connectAndGet("$testServerUrl/$endpoint")
        return Json.decodeFromString(frameText)
    }

    private suspend fun call(endpoint: String) {
        val connection = wsClient.connectWithTimeout("$testServerUrl/$endpoint")
        //connection.expectCloseFrame("no data for endpoint $endpoint") // commented because Ktor doesn't send CLOSE frame
        connection.expectNoMoreFrames("after close frame for endpoint $endpoint")
    }
}

private suspend fun HttpClient.connect(url: String) = webSocketSession {
    this.url.takeFrom(url)
}

private suspend fun HttpClient.connectWithTimeout(
    url: String,
    timeoutMillis: Long = 8000,
) = withTimeoutOrNull(timeoutMillis) { connect(url) } ?: fail("Timed out while connecting to $url")

private suspend fun HttpClient.connectAndGet(url: String): String {
    lateinit var result: String
    webSocket(url) {
        val dataFrame = expectTextFrame("JSON data for $url")
        result = dataFrame.text
        // expectCloseFrame("after JSON data for $url") // commented because Ktor doesn't send CLOSE frame
        expectNoMoreFrames("after close frame for $url")
    }
    return result
}

private suspend fun DefaultClientWebSocketSession.expectTextFrame(
    frameDescription: String,
    timeoutMillis: Long = DEFAULT_EXPECTED_FRAME_TIMEOUT_MILLIS,
) = expectFrame<WebSocketFrame.Text>(frameDescription, timeoutMillis)

private suspend fun DefaultClientWebSocketSession.expectCloseFrame(
    frameDescription: String = "no more data expected",
    timeoutMillis: Long = DEFAULT_EXPECTED_FRAME_TIMEOUT_MILLIS,
) = expectFrame<WebSocketFrame.Close>(frameDescription, timeoutMillis)

private suspend inline fun <reified T : WebSocketFrame> DefaultClientWebSocketSession.expectFrame(
    frameDescription: String,
    timeoutMillis: Long = DEFAULT_EXPECTED_FRAME_TIMEOUT_MILLIS,
): T {
    val frameType = T::class.simpleName
    val result = withTimeoutOrNull(timeoutMillis) { incoming.receiveCatching() }
    assertNotNull(result, "Timed out while waiting for $frameType frame ($frameDescription)")
    assertFalse(result.isClosed, "Expected $frameType frame ($frameDescription), but the channel was closed")
    assertFalse(result.isFailure, "Expected $frameType frame ($frameDescription), but the channel was failed: ${result.exceptionOrNull()}")

    val frame = result.getOrThrow()
    assertIs<T>(frame, "Should have received $frameType frame ($frameDescription), but got $frame")
    return frame
}

private suspend fun DefaultClientWebSocketSession.expectNoMoreFrames(
    eventDescription: String = "end of transmission",
    timeoutMillis: Long = 1000,
) {
    val result = withTimeoutOrNull(timeoutMillis) { incoming.receiveCatching() }
    assertNotNull(result, "Timed out while waiting for incoming frames channel to be closed ($eventDescription)")
    assertTrue(result.isClosed, "Frames channel should be closed now ($eventDescription), got $result")
}
