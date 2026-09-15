package coroutines.marcin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

/** Running "The problem with shared state" chapter snippets. */

private suspend fun massiveRun(action: suspend () -> Unit) =
    withContext(Dispatchers.Default) {
        repeat(1000) {
            launch { repeat(1000) { action() } }
        }
    }

@OptIn(ExperimentalCoroutinesApi::class)
fun main() = runBlocking {
    var counter = 0
    massiveRun { counter++ }
    println("no synchronization   : $counter")

    counter = 0
    val lock = Any()
    massiveRun { synchronized(lock) { counter++ } }
    println("synchronized(lock)   : $counter")

    val atomic = AtomicInteger()
    massiveRun { atomic.incrementAndGet() }
    println("incrementAndGet()    : ${atomic.get()}")

    val atomic2 = AtomicInteger()
    massiveRun { atomic2.set(atomic2.get() + 1) }
    println("set(get() + 1)       : ${atomic2.get()}")

    counter = 0
    val dispatcher = Dispatchers.IO.limitedParallelism(1)
    massiveRun { withContext(dispatcher) { counter++ } }
    println("limitedParallelism(1): $counter")

    counter = 0
    val mutex = Mutex()
    massiveRun { mutex.withLock { counter++ } }
    println("mutex.withLock       : $counter")

    // mutex stays locked across suspension
    class MutexRepo {
        private val messages = mutableListOf<String>()
        private val m = Mutex()
        suspend fun add(message: String) = m.withLock {
            delay(1000)
            messages.add(message)
        }
    }

    class DispatcherRepo {
        private val messages = mutableListOf<String>()
        private val d = Dispatchers.IO.limitedParallelism(1)
        suspend fun add(message: String) = withContext(d) {
            delay(1000)
            messages.add(message)
        }
    }

    val mRepo = MutexRepo()
    val t1 = measureTimeMillis {
        coroutineScope { repeat(5) { launch { mRepo.add("Message$it") } } }
    }
    println("\nmutex wrapping delay(1000) x5      : $t1")

    val dRepo = DispatcherRepo()
    val t2 = measureTimeMillis {
        coroutineScope { repeat(5) { launch { dRepo.add("Message$it") } } }
    }
    println("limitedParallelism(1) same thing   : $t2")

    println("\nSemaphore(2) over 5 coroutines:")
    coroutineScope {
        val semaphore = Semaphore(2)
        repeat(5) {
            launch {
                semaphore.withPermit {
                    delay(1000)
                    print(it)
                }
            }
        }
    }
    println()
}
