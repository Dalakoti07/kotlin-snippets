package coroutines.marcin

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * A coroutine is NOT bound to a thread. It suspends on one thread and may resume on another.
 *
 *   ./gradlew runMain -PmainClass=coroutines.marcin.SuspendResumeThreadExKt --console=plain
 */

/** Logs where we are right now, and shouts when the thread changed since the last log. */
private class ThreadLog(private val coroutine: String) {
    private var last: String? = null
    val seen = mutableListOf<String>()

    fun log(step: String) {
        val t = Thread.currentThread()
        val name = t.name
        seen += name
        val changed = last != null && last != name
        val marker = if (changed) "   <-- THREAD CHANGED (was $last)" else ""
        println("  [coroutine=$coroutine] $step thread=$name (id=${t.id})$marker")
        last = name
    }

    fun summary() {
        println("  [coroutine=$coroutine] ${seen.size} resumptions across ${seen.distinct().size} thread(s): ${seen.distinct().sorted()}")
    }
}

private suspend fun currentCoroutineName(): String =
    coroutineContext[CoroutineName]?.name ?: "?"

// ---------------------------------------------------------- A. the guaranteed case

private suspend fun demoA() {
    println("\n--- A. suspended on one thread, resumed BY another thread ---")
    withContext(Dispatchers.Unconfined + CoroutineName("A")) {
        val log = ThreadLog(currentCoroutineName())
        log.log("before suspend:")

        suspendCoroutine<Unit> { continuation ->
            println("  suspendCoroutine block running on thread=${Thread.currentThread().name}")
            // Somebody else -- a callback, a Netty event loop, a JDBC pool -- finishes our work
            thread(name = "resumer-thread") {
                println("  resuming from thread=${Thread.currentThread().name}")
                Thread.sleep(100)
                continuation.resume(Unit)   // the coroutine continues HERE, on this thread
            }
        }

        log.log("after resume:  ")
        log.summary()
    }
}

// ---------------------------------------------------------- B. the everyday case

private suspend fun demoB() {
    println("\n--- B. same thing with plain delay() on a BUSY Dispatchers.Default ---")
    coroutineScope {
        // Keep every worker busy in short bursts, so the free worker differs each time.
        val stop = System.currentTimeMillis() + 4_000
        val noise = List(Runtime.getRuntime().availableProcessors() * 3) {
            launch(Dispatchers.Default) {
                while (System.currentTimeMillis() < stop) {
                    val until = System.currentTimeMillis() + 20
                    var x = 1.0
                    while (System.currentTimeMillis() < until) x = x * 1.0000001 + 1.0
                    if (x < 0) println(x)
                }
            }
        }

        launch(Dispatchers.Default + CoroutineName("B")) {
            val log = ThreadLog(currentCoroutineName())
            repeat(20) { i ->
                log.log("step $i:".padEnd(9))
                delay(20)               // <-- suspension point
            }
            log.summary()
        }.join()

        noise.forEach { it.cancel() }
    }
}

// ---------------------------------------------------------- C. why you can't rely on it

private suspend fun demoC() {
    println("\n--- C. identical code on an IDLE pool: it usually stays put ---")
    withContext(Dispatchers.Default + CoroutineName("C")) {
        val log = ThreadLog(currentCoroutineName())
        repeat(20) { i ->
            log.log("step $i:".padEnd(9))
            delay(20)                   // <-- same suspension point as B
        }
        log.summary()
    }
    println("  'can move' is not 'always moves' -- which is why ThreadLocal bugs only show up under load")
}

suspend fun main() {
    println("CPU cores = ${Runtime.getRuntime().availableProcessors()}")
    demoA()
    demoB()
    demoC()
    println("\nTakeaway: a coroutine is a value that gets scheduled, not a thread that runs.")
    println("A ThreadLocal written before a suspension point may not be there after it.")
}
