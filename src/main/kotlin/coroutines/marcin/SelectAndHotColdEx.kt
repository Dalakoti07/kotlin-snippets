package coroutines.marcin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

/** Select chapter + Hot and cold data sources chapter. */

private suspend fun requestData1(): String { delay(100_000); return "Data1" }
private suspend fun requestData2(): String { delay(1000); return "Data2" }

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun selectRace() {
    println("\n=== select: race, with cancelChildren ===")
    val start = System.currentTimeMillis()
    val result = coroutineScope {
        select<String> {
            async { requestData1() }.onAwait { it }
            async { requestData2() }.onAwait { it }
        }.also { coroutineContext.cancelChildren() }
    }
    println("  got $result after ${System.currentTimeMillis() - start} ms")
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.produceString(s: String, time: Long) = produce {
    while (true) { delay(time); send(s) }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun selectReceive() = runBlocking {
    println("\n=== select: onReceive from two channels ===")
    val fooChannel = produceString("foo", 210L)
    val barChannel = produceString("BAR", 500L)
    repeat(7) {
        select<Unit> {
            fooChannel.onReceive { println("  From fooChannel: $it") }
            barChannel.onReceive { println("  From barChannel: $it") }
        }
    }
    coroutineContext.cancelChildren()
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun selectSend() = runBlocking {
    println("\n=== select: onSend to whichever channel has space ===")
    val c1 = Channel<Char>(capacity = 2)
    val c2 = Channel<Char>(capacity = 2)
    val sender = launch {
        for (c in 'A'..'H') {
            delay(400)
            select<Unit> {
                c1.onSend(c) { println("  Sent $c to 1") }
                c2.onSend(c) { println("  Sent $c to 2") }
            }
        }
    }
    val receiver = launch {
        while (true) {
            delay(1000)
            val c = select<String> {
                c1.onReceive { "$it from 1" }
                c2.onReceive { "$it from 2" }
            }
            println("  Received $c")
        }
    }
    sender.join()
    delay(3000)
    receiver.cancel()
}

// ---------------------------------------------------------- hot vs cold

private fun m(i: Int): Int { print("m$i "); return i * i }
private fun f(i: Int): Boolean { print("f$i "); return i >= 10 }

private fun listVsSequence() {
    println("\n=== list (hot) vs sequence (cold): operation order ===")
    print("  list     : ")
    listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map { m(it) }.find { f(it) }.let { print("-> $it") }
    println()
    print("  sequence : ")
    sequenceOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map { m(it) }.find { f(it) }.let { print("-> $it") }
    println()

    println("\n=== reuse: hot caches, cold recomputes ===")
    print("  list build   : ")
    val l = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map { m(it) }
    println()
    print("  l.find x3    : ")
    repeat(3) { print("${l.find { it > 10 }} ") }
    println()
    val s = sequenceOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map { m(it) }
    print("  s.find x3    : ")
    repeat(3) { print("[${s.find { it > 10 }}] ") }
    println()
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.makeChannel() = produce {
    println("  Channel started")
    for (i in 1..3) { delay(300); send(i) }
}

private fun makeFlow() = flow {
    println("  Flow started")
    for (i in 1..3) { delay(300); emit(i) }
}

private suspend fun channelVsFlow() = coroutineScope {
    println("\n=== Channel (hot) consumed twice ===")
    val channel = makeChannel()
    delay(300)
    println("  Calling channel...")
    for (value in channel) println("  $value")
    println("  Consuming again...")
    for (value in channel) println("  $value")

    println("\n=== Flow (cold) consumed twice ===")
    val flow = makeFlow()
    delay(300)
    println("  Calling flow...")
    flow.collect { println("  $it") }
    println("  Consuming again...")
    flow.collect { println("  $it") }
}

suspend fun main() {
    selectRace()
    selectReceive()
    selectSend()
    listVsSequence()
    channelVsFlow()
}
