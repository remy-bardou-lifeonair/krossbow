package org.hildan.krossbow.stomp

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.stomp.config.StompConfig
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.frame.StompCommand
import org.hildan.krossbow.stomp.frame.StompEvent
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.headers.*
import org.hildan.krossbow.stomp.heartbeats.HeartBeater
import org.hildan.krossbow.stomp.heartbeats.NO_HEART_BEATS
import org.hildan.krossbow.utils.generateUuid
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

internal class BaseStompSession(
    private val config: StompConfig,
    private val stompSocket: StompSocket,
    heartBeat: HeartBeat,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
) : StompSession {

    private val scope = CoroutineScope(coroutineContext + Job() + CoroutineName("stomp-session"))

    private val heartBeater = if (heartBeat != NO_HEART_BEATS) {
        HeartBeater(
            heartBeat = heartBeat,
            tolerance = config.heartBeatTolerance,
            sendHeartBeat = { stompSocket.sendHeartBeat() },
            onMissingHeartBeat = {
                val cause = MissingHeartBeatException(heartBeat.expectedPeriodMillis)
                sharedStompEvents.emit(StompEvent.Error(cause))
                stompSocket.close(cause)
                scope.cancel("STOMP session cancelled due to upstream error", cause = cause)
            },
        )
    } else {
        null
    }

    private val subscriptionsById = mutableMapOf<String, Channel<StompFrame.Message>>()
    private val receipts = mutableMapOf<String, CompletableDeferred<StompFrame.Receipt>>()

    init {
        scope.launch {
            stompSocket.incomingEvents
                .catch { failSubscribers(it) }
                .collect {
                    heartBeater?.notifyMsgReceived()
                    when (it) {
                        is StompEvent.HeartBeat -> Unit // ignore, already notified
                        is StompFrame.Message -> subscriptionsById[it.headers.subscription]?.send(it)
                        is StompFrame.Receipt -> receipts[it.headers.receiptId]?.complete(it)
                            ?: error("Missing deferred receipt for receipt ID ${it.headers.receiptId}")
                        else -> error("Unexpected STOMP frame $it")
                    }
                }
        }
    }

    private fun failSubscribers(e: Throwable) {
        subscriptionsById.values.forEach { sub -> sub.close(cause = e) }
        receipts.values.forEach { r -> r.completeExceptionally(e) }
    }

    @OptIn(FlowPreview::class) // for produceIn(scope)
    private suspend fun startSubscription(headers: StompSubscribeHeaders): ReceiveChannel<StompFrame.Message> {
        val subscriptionChannel = Channel<StompFrame.Message>(BUFFERED)
        subscriptionsById[headers.id] = subscriptionChannel
        prepareHeadersAndSendFrame(StompFrame.Subscribe(headers))
        return subscriptionChannel
    }

    override suspend fun send(headers: StompSendHeaders, body: FrameBody?): StompReceipt? {
        return prepareHeadersAndSendFrame(StompFrame.Send(headers, body))
    }

    private suspend fun prepareHeadersAndSendFrame(frame: StompFrame): StompReceipt? {
        maybeSetContentLength(frame)
        maybeSetAutoReceipt(frame)
        val receiptId = frame.headers.receipt
        if (receiptId == null) {
            sendStompFrame(frame)
            return null
        }
        sendAndWaitForReceipt(receiptId, frame)
        return StompReceipt(receiptId)
    }

    private fun maybeSetContentLength(frame: StompFrame) {
        if (config.autoContentLength && frame.headers.contentLength == null) {
            frame.headers.contentLength = frame.body?.bytes?.size ?: 0
        }
    }

    private fun maybeSetAutoReceipt(frame: StompFrame) {
        if (config.autoReceipt && frame.headers.receipt == null) {
            frame.headers.receipt = generateUuid()
        }
    }

    private suspend fun sendAndWaitForReceipt(receiptId: String, frame: StompFrame) {
        val deferredReceipt = CompletableDeferred<StompFrame.Receipt>()
        receipts[receiptId] = deferredReceipt
        stompSocket.sendStompFrame(frame)
        withTimeoutOrNull(frame.receiptTimeout) {
            deferredReceipt.await()
            receipts.remove(receiptId)
        } ?: throw LostReceiptException(receiptId, frame.receiptTimeout, frame)
    }

    private val StompFrame.receiptTimeout: Long
        get() = if (command == StompCommand.DISCONNECT) {
            config.disconnectTimeoutMillis
        } else {
            config.receiptTimeoutMillis
        }

    override suspend fun subscribe(headers: StompSubscribeHeaders): Flow<StompFrame.Message> {
        val headersWithId = headers.withId()

        return startSubscription(headersWithId)
            .consumeAsFlow()
            .onCompletion {
                when (it) {
                    // If the consumer was cancelled or an exception occurred downstream, the STOMP session keeps going
                    // so we want to unsubscribe this failed subscription.
                    // Note that calling .first() actually cancels the flow with CancellationException, so it's
                    // covered here.
                    is CancellationException -> {
                        if (scope.isActive) {
                            unsubscribe(headersWithId.id)
                        } else {
                            // The whole session is cancelled, the web socket must be already closed
                        }
                    }
                    // If the flow completes normally, it means the frames channel is closed, and so is the web socket
                    // connection. We can't send an unsubscribe frame in this case.
                    // If an exception is thrown upstream, it means there was a STOMP or web socket error and we can't
                    // unsubscribe either.
                    else -> Unit
                }
            }
    }

    private suspend fun unsubscribe(subscriptionId: String) {
        sendStompFrame(StompFrame.Unsubscribe(StompUnsubscribeHeaders(id = subscriptionId)))
        subscriptionsById[subscriptionId]?.close()
        subscriptionsById.remove(subscriptionId)
    }

    override suspend fun ack(ackId: String, transactionId: String?) {
        sendStompFrame(StompFrame.Ack(StompAckHeaders(ackId, transactionId)))
    }

    override suspend fun nack(ackId: String, transactionId: String?) {
        sendStompFrame(StompFrame.Nack(StompNackHeaders(ackId, transactionId)))
    }

    override suspend fun begin(transactionId: String) {
        sendStompFrame(StompFrame.Begin(StompBeginHeaders(transactionId)))
    }

    override suspend fun commit(transactionId: String) {
        sendStompFrame(StompFrame.Commit(StompCommitHeaders(transactionId)))
    }

    override suspend fun abort(transactionId: String) {
        sendStompFrame(StompFrame.Abort(StompAbortHeaders(transactionId)))
    }

    private suspend fun sendStompFrame(frame: StompFrame) {
        stompSocket.sendStompFrame(frame)
        heartBeater?.notifyMsgSent()
    }

    override suspend fun disconnect() {
        if (config.gracefulDisconnect) {
            sendDisconnectFrameAndWaitForReceipt()
        }
        stompSocket.close()
        sharedStompEvents.emit(StompEvent.Close)
        scope.cancel("STOMP session disconnected")
    }

    private suspend fun sendDisconnectFrameAndWaitForReceipt() {
        try {
            val receiptId = generateUuid()
            val disconnectFrame = StompFrame.Disconnect(StompDisconnectHeaders(receiptId))
            sendAndWaitForReceipt(receiptId, disconnectFrame)
        } catch (e: LostReceiptException) {
            // Sometimes the server closes the connection too quickly to send a RECEIPT, which is not really an error
            // http://stomp.github.io/stomp-specification-1.2.html#Connection_Lingering
        }
    }
}

private fun StompSubscribeHeaders.withId(): StompSubscribeHeaders {
    // we can't use the delegated id property here, because it would crash if the underlying header is absent
    val existingId = get(HeaderNames.ID)
    if (existingId != null) {
        return this
    }
    val rawHeadersCopy = HashMap(this)
    rawHeadersCopy[HeaderNames.ID] = generateUuid()
    return StompSubscribeHeaders(rawHeadersCopy.asStompHeaders())
}
