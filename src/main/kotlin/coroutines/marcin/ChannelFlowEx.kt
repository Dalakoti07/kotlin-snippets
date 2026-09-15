package coroutines.marcin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * Why channelFlow exists, when flow {} was already there.
 *
 *   ./gradlew runMain -PmainClass=coroutines.marcin.ChannelFlowExKt --args="1"
 */

private val t0 = System.currentTimeMillis()
private fun log(msg: String) {
    val ms = System.currentTimeMillis() - t0
    println("  %5d ms [%-22s] %s".format(ms, Thread.currentThread().name, msg))
}

// ---------------------------------------------------- 1. the wall flow {} hits

private suspend fun exp1() {
    println("\n=== 1. flow {} forbids emitting from another coroutine ===")

    val broken: Flow<Int> = flow {
        coroutineScope {
            launch { emit(1) }        // compiles fine -- blows up at runtime
            launch { emit(2) }
        }
    }
    try {
        broken.collect { log("got $it") }
    } catch (e: IllegalStateException) {
        println("  IllegalStateException:")
        e.message?.lines()?.forEach { println("    $it") }
    }

    println("\n  the same shape with channelFlow:")
    val works: Flow<Int> = channelFlow {
        launch { send(1) }            // send() is thread-safe, and we are given a scope
        launch { send(2) }
    }
    println("  collected: ${works.toList().sorted()}")
    println("  => flow{}.emit is confined to ONE coroutine; channelFlow.send is not")
}

// ---------------------------------------------------- 2. fan-in of several sources

private suspend fun exp2() {
    println("\n=== 2. fan-in: several producers with different cadences, one flow ===")

    val merged: Flow<String> = channelFlow {
        launch { repeat(4) { delay(100); send("fast-$it") } }
        launch { repeat(2) { delay(250); send("slow-$it") } }
    }
    merged.collect { log("collected $it") }
    println("  => a single flow {} could only interleave these by hand-rolling a state machine")
}

// ---------------------------------------------------- 3. parallel work, emit as ready

private suspend fun exp3cf() {
    println("\n=== 3. fan-out: run work in parallel, emit results as they finish ===")
    val jobs = listOf(300L, 100L, 200L, 50L)

    val sequential = flow {
        for (d in jobs) { delay(d); emit(d) }
    }
    val seqMs = measureTimeMillis { println("  flow{}       order=${sequential.toList()}") }
    println("  flow{}       took ${seqMs}ms  (sum of all durations)")

    val parallel = channelFlow {
        for (d in jobs) launch { delay(d); send(d) }
    }
    val parMs = measureTimeMillis { println("  channelFlow  order=${parallel.toList()}") }
    println("  channelFlow  took ${parMs}ms  (max of all durations, results in completion order)")
}

// ---------------------------------------------------- 4. still COLD

private suspend fun exp4cf() {
    println("\n=== 4. channelFlow is still COLD (a common misconception) ===")
    var blockRuns = 0

    val cold = channelFlow {
        blockRuns++
        log("channelFlow block STARTED (run #$blockRuns)")
        send(1); send(2)
    }

    log("flow created -- nothing has run yet, blockRuns=$blockRuns")
    delay(200)
    log("still nothing: blockRuns=$blockRuns")

    println("  first collector:  ${cold.toList()}")
    println("  second collector: ${cold.toList()}")
    println("  block ran $blockRuns times -- once per collection, like any cold flow")
    println("  => channelFlow is NOT a SharedFlow; it does not broadcast or replay")
}

// ---------------------------------------------------- 5. callback bridging

private class SensorApi {
    private var listener: ((Int) -> Unit)? = null
    var registered = false; private set

    fun register(l: (Int) -> Unit) {
        listener = l; registered = true
        log("SensorApi: listener registered")
        Thread(
            {
                var i = 0
                while (registered) { Thread.sleep(80); listener?.invoke(i++) }
            },
            "sensor-hw-thread"
        ).apply { isDaemon = true }.start()
    }

    fun unregister() { registered = false; listener = null; log("SensorApi: listener UNregistered") }
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun exp5cf() {
    println("\n=== 5. callbackFlow: bridging a callback API that fires on a foreign thread ===")
    val api = SensorApi()

    val readings: Flow<Int> = callbackFlow {
        api.register { value -> trySend(value) }   // called from sensor-hw-thread
        awaitClose { api.unregister() }            // mandatory: cleanup on cancellation
    }

    readings.take(4).collect { log("reading $it") }
    delay(100)
    println("  listener still registered after collection ended? ${api.registered}")
    println("  => flow{} cannot do this at all: the callback fires on a thread that never called emit")
}

// ---------------------------------------------------- 6. the cost: buffering

private suspend fun exp6cf() {
    println("\n=== 6. the side effect you inherit: channelFlow buffers by default ===")

    println("  flow{} -- producer and consumer are lock-step:")
    flow {
        repeat(4) { log("    emit $it"); emit(it) }
    }.collect { delay(150); log("    consumed $it") }

    println("  channelFlow -- producer runs ahead into the channel buffer:")
    channelFlow {
        repeat(4) { log("    send $it"); send(it) }
    }.collect { delay(150); log("    consumed $it") }

    println("  => channelFlow implies a buffer (default 64). You can get that from flow{}.buffer() too;")
    println("     buffering is NOT the reason channelFlow exists -- concurrent send() is.")
}

// ---------------------------------------------------- 7. withContext inside

private suspend fun exp7cf() {
    println("\n=== 7. emitting values produced in a different context ===")

    val broken = flow {
        withContext(Dispatchers.IO) { emit(1) }   // context preservation violation
    }
    try {
        broken.collect { log("got $it") }
    } catch (e: IllegalStateException) {
        println("  flow{} -> IllegalStateException:")
        e.message?.lines()?.forEach { println("    $it") }
    }

    val fine = channelFlow {
        withContext(Dispatchers.IO) { send(1) }
    }
    println("  channelFlow -> collected ${fine.toList()}")
    println("  => send() carries no context invariant, so it is legal from anywhere")
}

// ---------------------------------------------------- runner

suspend fun main(args: Array<String>) {
    when (args.firstOrNull()) {
        "1" -> exp1()
        "2" -> exp2()
        "3" -> exp3cf()
        "4" -> exp4cf()
        "5" -> exp5cf()
        "6" -> exp6cf()
        "7" -> exp7cf()
        else -> { exp1(); exp2(); exp3cf(); exp4cf(); exp5cf(); exp6cf(); exp7cf() }
    }
}
