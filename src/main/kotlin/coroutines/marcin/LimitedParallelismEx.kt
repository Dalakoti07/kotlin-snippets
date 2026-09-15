package coroutines.marcin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

/**
 * Experiments for `Dispatchers.Default.limitedParallelism(5)`.
 *
 * Run one at a time:
 *   ./gradlew runMain -PmainClass=coroutines.marcin.LimitedParallelismExKt --args="1"
 * or all of them with no --args.
 */

// Created ONCE and stored -- this is the correct usage (see experiment 5).
@OptIn(ExperimentalCoroutinesApi::class)
private val fiveWide: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(5)

// ---------------------------------------------------------------- helpers

private class Meter(val label: String) {
    private val inFlight = AtomicInteger()
    private val peak = AtomicInteger()
    private val threads = ConcurrentHashMap.newKeySet<String>()

    fun enter() {
        val now = inFlight.incrementAndGet()
        peak.accumulateAndGet(now) { a, b -> maxOf(a, b) }
        threads += Thread.currentThread().name
    }

    fun exit() {
        inFlight.decrementAndGet()
    }

    fun threadNames(): Set<String> = threads

    fun report(extra: String = "") {
        println("  [$label] peak concurrency = ${peak.get()}, distinct threads = ${threads.size} $extra")
    }
}

private inline fun <T> Meter.measure(block: () -> T): T {
    enter()
    try {
        return block()
    } finally {
        exit()
    }
}

/** Occupies a thread with real CPU work for [millis]. No locks, no shared state. */
private fun burnCpu(millis: Long) {
    val end = System.currentTimeMillis() + millis
    var x = 1.0
    while (System.currentTimeMillis() < end) {
        x = x * 1.0000001 + 1.0
        if (x > 1e18) x = 1.0
    }
    if (x < 0) println(x) // keep the JIT from deleting the loop
}

// ------------------------------------------------- 1. what it actually limits

private suspend fun exp1() {
    println("\n=== 1. Dispatchers.Default vs Dispatchers.Default.limitedParallelism(5) ===")
    println("40 CPU tasks of 50ms each")
    val cases = listOf<Pair<String, CoroutineDispatcher>>(
        "Dispatchers.Default" to Dispatchers.Default,
        "limitedParallelism(5)" to fiveWide,
    )
    for ((name, dispatcher) in cases) {
        val meter = Meter(name)
        val ms = measureTimeMillis {
            coroutineScope {
                repeat(40) { launch(dispatcher) { meter.measure { burnCpu(50) } } }
            }
        }
        meter.report("wall time = ${ms}ms")
    }
    println("  => the view runs at most 5 at once, so it takes ~40/5 * 50ms instead of ~40/cores * 50ms")
}

// ------------------------------------------------- 2. it is a VIEW, not a pool

private suspend fun exp2() {
    println("\n=== 2. It is a VIEW: the threads come from the same Default pool ===")
    val viaView = Meter("through limitedParallelism(5)")
    val viaDefault = Meter("through Dispatchers.Default")
    coroutineScope {
        repeat(30) { launch(fiveWide) { viaView.measure { burnCpu(30) } } }
        repeat(30) { launch(Dispatchers.Default) { viaDefault.measure { burnCpu(30) } } }
    }
    viaView.report()
    viaDefault.report()
    val shared = viaView.threadNames() intersect viaDefault.threadNames()
    println("  threads used by BOTH paths: ${shared.size} -> ${shared.sorted()}")
    println("  => no threads were created, and Dispatchers.Default itself was NOT throttled to 5")
}

// ------------------------------------------------- 3. one-wide != one thread

private suspend fun exp3() {
    println("\n=== 3. limitedParallelism(1) is NOT a single thread ===")
    @OptIn(ExperimentalCoroutinesApi::class)
    val single = Dispatchers.Default.limitedParallelism(1)

    val meter = Meter("limitedParallelism(1)")
    coroutineScope {
        // Saturate every worker of the Default pool for the whole experiment,
        // so the worker that happens to be free varies from resumption to resumption.
        val stop = System.currentTimeMillis() + 3_000
        val cores = Runtime.getRuntime().availableProcessors()
        val noise = List(cores * 3) {
            launch(Dispatchers.Default) {
                while (System.currentTimeMillis() < stop) burnCpu(20)
            }
        }

        // ONE coroutine, many resumptions: where does it land each time?
        launch(single) {
            val seen = mutableListOf<String>()
            repeat(40) {
                meter.measure { seen += Thread.currentThread().name }
                delay(5)
            }
            println("  ONE coroutine, 40 resumptions, distinct threads: ${seen.distinct().sorted()}")
        }.join()

        noise.forEach { it.cancel() }
    }
    meter.report()
    println("  => concurrency never exceeds 1, but the coroutine is not pinned to one thread")
}

// ------------------------------------------------- 4. IO is special-cased

private suspend fun exp4() {
    println("\n=== 4. Dispatchers.IO.limitedParallelism(100) bypasses IO's 64-thread cap ===")
    @OptIn(ExperimentalCoroutinesApi::class)
    val io100 = Dispatchers.IO.limitedParallelism(100)
    val cases = listOf<Pair<String, CoroutineDispatcher>>(
        "Dispatchers.IO" to Dispatchers.IO,
        "Dispatchers.IO.limitedParallelism(100)" to io100,
    )
    for ((name, dispatcher) in cases) {
        val meter = Meter(name)
        coroutineScope {
            repeat(100) { launch(dispatcher) { meter.measure { Thread.sleep(500) } } }
        }
        meter.report()
    }
    println("  => IO tops out at 64; a limitedParallelism view of IO gets its OWN independent budget")
    println("     (Default.limitedParallelism(n) can never exceed the core count -- opposite behaviour)")
}

// ------------------------------------------------- 5. the footgun

private suspend fun exp5() {
    println("\n=== 5. FOOTGUN: building the limiter inside the loop ===")
    @OptIn(ExperimentalCoroutinesApi::class)
    val stored = Dispatchers.IO.limitedParallelism(2)

    val good = Meter("one stored limiter, limit 2")
    coroutineScope { repeat(20) { launch(stored) { good.measure { Thread.sleep(200) } } } }
    good.report()

    val bad = Meter("a NEW limiter per launch, limit 2")
    coroutineScope {
        repeat(20) {
            @OptIn(ExperimentalCoroutinesApi::class)
            val fresh = Dispatchers.IO.limitedParallelism(2)
            launch(fresh) { bad.measure { Thread.sleep(200) } }
        }
    }
    bad.report()
    println("  => every call returns an INDEPENDENT limiter, so 20 of them means no limit at all")
}

// ------------------------------------------------- 6. as a mutex, and its limit

private suspend fun exp6() {
    println("\n=== 6. limitedParallelism(1) as a mutex -- and where it stops being one ===")
    @OptIn(ExperimentalCoroutinesApi::class)
    val single = Dispatchers.Default.limitedParallelism(1)

    var c1 = 0
    coroutineScope { repeat(1_000) { launch(Dispatchers.Default) { repeat(1_000) { c1++ } } } }
    println("  Dispatchers.Default            expected 1000000, got $c1")

    var c2 = 0
    coroutineScope { repeat(1_000) { launch(single) { repeat(1_000) { c2++ } } } }
    println("  limitedParallelism(1)          expected 1000000, got $c2")

    var c3 = 0
    coroutineScope {
        repeat(1_000) {
            launch(single) {
                val tmp = c3
                delay(1)          // <-- releases the single slot
                c3 = tmp + 1
            }
        }
    }
    println("  limitedParallelism(1) + delay  expected    1000, got $c3")
    println("  => it serialises EXECUTION, not a critical section: any suspension hands the slot away")
}

// ------------------------------------------------- 7. WHY IO ignores its own 64

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun exp7() {
    println("\n=== 7. Why Dispatchers.IO.limitedParallelism ignores the 64 cap ===")

    // (a) A view of Dispatchers.IO is NOT nested inside IO's own 64-wide limiter.
    val io100 = Dispatchers.IO.limitedParallelism(100)

    // (b) A view of a view IS nested normally -- proof the special case lives only on Dispatchers.IO.
    val io10 = Dispatchers.IO.limitedParallelism(10)
    val io10then100 = io10.limitedParallelism(100)
    println("  io10.limitedParallelism(100) === io10 ? ${io10 === io10then100}")

    val cases = listOf<Pair<String, CoroutineDispatcher>>(
        "IO.limitedParallelism(100)" to io100,
        "IO.limitedParallelism(10).limitedParallelism(100)" to io10then100,
    )
    for ((name, dispatcher) in cases) {
        val meter = Meter(name)
        coroutineScope { repeat(100) { launch(dispatcher) { meter.measure { Thread.sleep(300) } } } }
        meter.report()
    }

    // (c) IO's blocking threads and Default's CPU threads are the SAME pool.
    val blocking = Meter("IO blocking tasks")
    val cpu = Meter("Default CPU tasks")
    coroutineScope {
        repeat(80) { launch(io100) { blocking.measure { Thread.sleep(400) } } }
        repeat(40) { launch(Dispatchers.Default) { cpu.measure { burnCpu(30) } } }
    }
    blocking.report()
    cpu.report()
    val sample = blocking.threadNames().sorted().take(3)
    println("  sample IO thread names: $sample")
    println("  Default kept its full ${Runtime.getRuntime().availableProcessors()} CPU slots while 80 threads were blocked")
    println("  => one CoroutineScheduler; blocking tasks do not hold a CPU permit, so the pool grows past the core count")
}

// ---------------------------------------------------------------- runner

suspend fun main(args: Array<String>) {
    println("CPU cores = ${Runtime.getRuntime().availableProcessors()}")
    when (args.firstOrNull()) {
        "1" -> exp1()
        "2" -> exp2()
        "3" -> exp3()
        "4" -> exp4()
        "5" -> exp5()
        "6" -> exp6()
        "7" -> exp7()
        else -> {
            exp1(); exp2(); exp3(); exp4(); exp5(); exp6(); exp7()
        }
    }
}
