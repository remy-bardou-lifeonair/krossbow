package org.hildan.krossbow.stomp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import org.hildan.krossbow.stomp.config.HeartBeat

internal class HeartBeater(
    val heartBeat: HeartBeat,
    val sendHeartBeat: suspend () -> Unit,
    val onMissingHeartBeat: suspend () -> Unit
) {
    private val outgoing = Ticker(heartBeat.minSendPeriodMillis.toLong(), sendHeartBeat)
    private val incoming = Ticker(heartBeat.expectedPeriodMillis.toLong(), onMissingHeartBeat)

    fun startIn(scope: CoroutineScope) {
        outgoing.startIn(scope)
        incoming.startIn(scope)
    }

    suspend fun notifyMsgSent() {
        outgoing.reset()
    }

    suspend fun notifyMsgReceived() {
        incoming.reset()
    }
}

private class Ticker(
    private val periodMillis: Long,
    private val onTick: suspend () -> Unit
) {
    private val resetter = Channel<Unit>()

    fun startIn(scope: CoroutineScope): Job = scope.launch {
        while (isActive) {
            select<Unit> {
                resetter.onReceive { }
                onTimeout(periodMillis, onTick)
            }
        }
    }

    suspend fun reset() {
        resetter.send(Unit)
    }
}
