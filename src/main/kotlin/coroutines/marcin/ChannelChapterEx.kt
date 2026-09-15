package coroutines.marcin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** Running the Channel chapter's own snippets. */

private var t0 = System.currentTimeMillis()
private fun log(m: String) = println("  %4d ms  %s".format(System.currentTimeMillis() - t0, m))
private fun reset() { t0 = System.currentTimeMillis() }

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun capacities() = coroutineScope {
    val cases = listOf(
        "UNLIMITED" to Channel.UNLIMITED,
        "capacity = 3" to 3,
        "RENDEZVOUS (default)" to Channel.RENDEZVOUS,
        "CONFLATED" to Channel.CONFLATED,
    )
    for ((name, cap) in cases) {
        println("\n--- $name ---")
        reset()
        coroutineScope {
            val channel = produce(capacity = cap) {
                repeat(5) { index ->
                    send(index * 2)
                    delay(100)
                    log("Sent")
                }
            }
            delay(1000)
            for (element in channel) {
                log("received $element")
                delay(1000)
            }
        }
    }
}

private suspend fun bufferOverflow() = coroutineScope {
    println("\n--- capacity = 2, DROP_OLDEST ---")
    reset()
    val channel = Channel<Int>(
        capacity = 2,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    launch {
        repeat(5) { index ->
            channel.send(index * 2)
            delay(100)
            log("Sent")
        }
        channel.close()
    }
    delay(1000)
    for (element in channel) {
        log("received $element")
        delay(1000)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.produceNumbers() = produce {
    repeat(9) {
        delay(100)
        send(it)
    }
}

private fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    for (msg in channel) {
        println("  #$id received $msg")
    }
}

private suspend fun fanOut() = coroutineScope {
    println("\n--- fan-out: 3 processors, one channel ---")
    val channel = produceNumbers()
    repeat(3) { id ->
        delay(10)
        launchProcessor(id, channel)
    }
}

private suspend fun sendString(channel: SendChannel<String>, text: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(text)
    }
}

private fun fanIn() = runBlocking {
    println("\n--- fan-in: two senders, one channel ---")
    reset()
    val channel = Channel<String>()
    launch { sendString(channel, "foo", 200L) }
    launch { sendString(channel, "BAR!", 500L) }
    repeat(8) {
        log(channel.receive())
    }
    coroutineContext.cancelChildren()
}

suspend fun main() {
    capacities()
    bufferOverflow()
    fanOut()
    fanIn()
}
