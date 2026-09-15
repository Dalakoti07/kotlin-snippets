package coroutines.marcin

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/** SharedFlow and StateFlow chapter. */

private suspend fun replayCache() = coroutineScope {
    println("\n=== replay = 2 ===")
    val f = MutableSharedFlow<String>(replay = 2)
    f.emit("Message1"); f.emit("Message2"); f.emit("Message3")
    println("  replayCache = ${f.replayCache}")
    val job = launch { f.collect { println("  #1 received $it") } }
    delay(100)
    f.resetReplayCache()
    println("  after reset = ${f.replayCache}")
    job.cancel()
    coroutineContext.cancelChildren()
}

private suspend fun shareInEagerly() = coroutineScope {
    println("\n=== shareIn Eagerly (subscriber arrives late, replay = 0) ===")
    val flow = flowOf("A", "B", "C")
    val shared: SharedFlow<String> = flow.shareIn(this, SharingStarted.Eagerly)
    delay(100)
    val job = launch { shared.collect { println("  #1 $it") } }
    println("  Done -- nothing was received")
    delay(100)
    job.cancel()
    coroutineContext.cancelChildren()
}

private suspend fun shareInLazily() = coroutineScope {
    println("\n=== shareIn Lazily ===")
    val flow1 = flowOf("A", "B", "C")
    val flow2 = flowOf("D").onEach { delay(600) }
    val shared = merge(flow1, flow2).shareIn(this, SharingStarted.Lazily)
    delay(100)
    val j1 = launch { shared.collect { println("  #1 $it") } }
    delay(600)
    val j2 = launch { shared.collect { println("  #2 $it") } }
    delay(300)
    j1.cancel(); j2.cancel()
    coroutineContext.cancelChildren()
}

private suspend fun shareInWhileSubscribed() = coroutineScope {
    println("\n=== shareIn WhileSubscribed ===")
    val flow = flowOf("A", "B", "C", "D")
        .onStart { println("  Started") }
        .onCompletion { println("  Finished") }
        .onEach { delay(300) }

    val shared = flow.shareIn(this, SharingStarted.WhileSubscribed())

    delay(900)
    launch { println("  #1 ${shared.first()}") }
    launch { println("  #2 ${shared.take(2).toList()}") }
    delay(900)
    launch { println("  #3 ${shared.first()}") }
    delay(900)
    coroutineContext.cancelChildren()
}

private suspend fun stateFlowBasics() = coroutineScope {
    println("\n=== StateFlow: value + collect ===")
    val state = MutableStateFlow("A")
    println("  value = ${state.value}")
    val j1 = launch { state.collect { println("  Value changed to $it") } }
    delay(300)
    state.value = "B"
    delay(300)
    val j2 = launch { state.collect { println("  and now it is $it") } }
    delay(300)
    state.value = "C"
    delay(100)
    j1.cancel(); j2.cancel()
    coroutineContext.cancelChildren()
}

private suspend fun stateFlowConflates() = coroutineScope {
    println("\n=== StateFlow is CONFLATED: slow collector misses states ===")
    val state = MutableStateFlow('X')
    val producer = launch {
        for (c in 'A'..'E') {
            delay(300)
            state.value = c
        }
    }
    val collector = launch {
        state.collect {
            delay(1000)
            println("  $it")
        }
    }
    producer.join()
    delay(2000)
    collector.cancel()
    coroutineContext.cancelChildren()
}

private suspend fun stateInDemo() = coroutineScope {
    println("\n=== stateIn (suspending variant) ===")
    val flow = flowOf("A", "B", "C")
        .onEach { delay(300) }
        .onEach { println("  Produced $it") }
    val stateFlow: StateFlow<String> = flow.stateIn(this)
    println("  Listening")
    println("  value = ${stateFlow.value}")
    val job = launch { stateFlow.collect { println("  Received $it") } }
    delay(900)
    job.cancel()

    println("\n=== stateIn (non-suspending, with initialValue) ===")
    val flow2 = flowOf("A", "B").onEach { delay(300) }.onEach { println("  Produced $it") }
    val sf2 = flow2.stateIn(this, SharingStarted.Lazily, "Empty")
    println("  value = ${sf2.value}")
    delay(600)
    val job2 = launch { sf2.collect { println("  Received $it") } }
    delay(900)
    job2.cancel()
    coroutineContext.cancelChildren()
}

suspend fun main() {
    replayCache()
    shareInEagerly()
    shareInLazily()
    shareInWhileSubscribed()
    stateFlowBasics()
    stateFlowConflates()
    stateInDemo()
}
