package coroutines.marcin

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import kotlin.random.Random

/** Flow introduction + Understanding Flow chapters. */

// ---------- Flow introduction: Sequence blocks, Flow suspends

private fun bookSequence(): Sequence<String> = sequence {
    repeat(3) {
        Thread.sleep(300)
        yield("User$it")
    }
}

private fun bookFlow(): Flow<String> = flow {
    repeat(3) {
        delay(300)
        emit("User$it")
    }
}

@Suppress("DEPRECATION")
private suspend fun sequenceBlocksFlowDoesNot() {
    println("\n=== Sequence: forEach BLOCKS the thread ===")
    withContext(newSingleThreadContext("main")) {
        launch {
            repeat(3) { delay(30); println("  Processing on coroutine") }
        }
        bookSequence().forEach { println("  $it") }
    }

    println("\n=== Flow: collect SUSPENDS ===")
    withContext(newSingleThreadContext("main")) {
        launch {
            repeat(3) { delay(30); println("  Processing on coroutine") }
        }
        bookFlow().collect { println("  $it") }
    }
}

// ---------- Understanding Flow: three counter variants

/** state INSIDE the flow step -- safe */
private fun Flow<*>.counterInside() = flow {
    var counter = 0
    collect {
        counter++
        List(100) { Random.nextLong() }.shuffled().sorted()
        emit(counter)
    }
}

/** state captured in the FUNCTION -- shared per flow instance */
private fun Flow<*>.counterCaptured(): Flow<Int> {
    var counter = 0
    return this.map {
        counter++
        List(100) { Random.nextLong() }.shuffled().sorted()
        counter
    }
}

private var topLevelCounter = 0

/** state at TOP LEVEL -- shared by everything */
private fun Flow<*>.counterTopLevel(): Flow<Int> = this.map {
    topLevelCounter++
    List(100) { Random.nextLong() }.shuffled().sorted()
    topLevelCounter
}

private suspend fun sharedState() = coroutineScope {
    println("\n=== state inside the flow builder (safe) ===")
    val a1 = List(1000) { "$it" }.asFlow()
    val a2 = List(1000) { "$it" }.asFlow().counterInside()
    coroutineScope {
        launch { println("  f1.counter().last() = ${a1.counterInside().last()}") }
        launch { println("  f1.counter().last() = ${a1.counterInside().last()}") }
        launch { println("  f2.last()           = ${a2.last()}") }
        launch { println("  f2.last()           = ${a2.last()}") }
    }

    println("\n=== state captured in the function (shared per flow) ===")
    val b1 = List(1000) { "$it" }.asFlow()
    val b2 = List(1000) { "$it" }.asFlow().counterCaptured()
    coroutineScope {
        launch { println("  f1.counter().last() = ${b1.counterCaptured().last()}") }
        launch { println("  f1.counter().last() = ${b1.counterCaptured().last()}") }
        launch { println("  f2.last()           = ${b2.last()}") }
        launch { println("  f2.last()           = ${b2.last()}") }
    }

    println("\n=== state at top level (shared by everything) ===")
    val c1 = List(1000) { "$it" }.asFlow()
    val c2 = List(1000) { "$it" }.asFlow().counterTopLevel()
    coroutineScope {
        launch { println("  f1.counter().last() = ${c1.counterTopLevel().last()}") }
        launch { println("  f1.counter().last() = ${c1.counterTopLevel().last()}") }
        launch { println("  f2.last()           = ${c2.last()}") }
        launch { println("  f2.last()           = ${c2.last()}") }
    }
}

suspend fun main() {
    sequenceBlocksFlowDoesNot()
    sharedState()
}
