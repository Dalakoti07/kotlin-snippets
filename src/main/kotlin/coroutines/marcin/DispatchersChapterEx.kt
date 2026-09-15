package coroutines.marcin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/** Running the Dispatchers chapter's own snippets on this machine. */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main(): Unit = coroutineScope {
    println("cores = ${Runtime.getRuntime().availableProcessors()}")

    // --- book: withContext(IO) from Default usually keeps the same thread
    println("\n[same-thread check]")
    launch(Dispatchers.Default) {
        println("  " + Thread.currentThread().name)
        withContext(Dispatchers.IO) {
            println("  " + Thread.currentThread().name)
        }
    }.join()

    // --- book: shared state on a multi-threaded dispatcher
    println("\n[shared state]")
    var i = 0
    coroutineScope { repeat(10_000) { launch(Dispatchers.IO) { i++ } } }
    println("  Dispatchers.IO           i = $i   (expected 10000)")

    var j = 0
    val single = Dispatchers.Default.limitedParallelism(1)
    coroutineScope { repeat(10_000) { launch(single) { j++ } } }
    println("  limitedParallelism(1)    j = $j   (expected 10000)")

    // --- book: single thread serialises blocking calls
    println("\n[blocking on limitedParallelism(1)]")
    val job = Job()
    repeat(5) { launch(single + job) { Thread.sleep(1000) } }
    job.complete()
    println("  Took ${measureTimeMillis { job.join() }}")

    // --- book: IO vs IO.limitedParallelism(100)
    println("\n[IO vs IO.limitedParallelism(100)]")
    coroutineScope {
        launch { printCoroutinesTime(Dispatchers.IO) }
        launch { printCoroutinesTime(Dispatchers.IO.limitedParallelism(100)) }
    }
}

private suspend fun printCoroutinesTime(dispatcher: CoroutineDispatcher) {
    val test = measureTimeMillis {
        coroutineScope { repeat(100) { launch(dispatcher) { Thread.sleep(1000) } } }
    }
    println("  $dispatcher took: $test")
}
