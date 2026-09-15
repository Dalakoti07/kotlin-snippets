package coroutines.marcin

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/** Flow lifecycle functions chapter. */

private class MyError : Throwable("My error")

private suspend fun lifecycle() = coroutineScope {
    println("\n=== onStart emits downstream ===")
    flowOf(1, 2).onEach { delay(200) }.onStart { emit(0) }.collect { println("  $it") }

    println("\n=== onCompletion fires on cancellation too ===")
    val job = launch {
        flowOf(1, 2)
            .onEach { delay(200) }
            .onCompletion { println("  Completed") }
            .collect { println("  $it") }
    }
    delay(220)
    job.cancel()
    delay(100)

    println("\n=== onEmpty supplies a default ===")
    flow<List<Int>> { delay(200) }.onEmpty { emit(emptyList()) }.collect { println("  $it") }

    println("\n=== catch: onEach does NOT see the exception ===")
    val f = flow { emit(1); emit(2); throw MyError() }
    f.onEach { println("  Got $it") }
        .catch { println("  Caught $it") }
        .collect { println("  Collected $it") }

    println("\n=== catch can emit a replacement ===")
    flow<String> { emit("Message1"); throw MyError() }
        .catch { emit("Error") }
        .collect { println("  Collected $it") }

    println("\n=== catch does NOT protect the terminal operation ===")
    try {
        flowOf("Message1", "Message2")
            .onStart { println("  Before") }
            .catch { println("  Caught $it") }
            .collect { throw MyError() }
    } catch (e: MyError) {
        println("  escaped to try/catch: $e")
    }

    println("\n=== ...so move the work into onEach ===")
    flowOf("Message1", "Message2")
        .onStart { println("  Before") }
        .onEach { throw MyError() }
        .catch { println("  Caught $it") }
        .collect()
}

private suspend fun present(place: String, message: String) {
    val name = coroutineContext[CoroutineName]?.name
    println("  [$name] $message on $place")
}

private fun messagesFlow(): Flow<String> = flow {
    present("flow builder", "Message")
    emit("Message")
}

private suspend fun context() {
    println("\n=== context comes from collect ===")
    val users = flow {
        repeat(2) {
            val name = currentName()
            emit("User$it in $name")
        }
    }
    withContext(CoroutineName("Name1")) { users.collect { println("  $it") } }
    withContext(CoroutineName("Name2")) { users.collect { println("  $it") } }

    println("\n=== flowOn affects only what is UPSTREAM ===")
    withContext(CoroutineName("Name1")) {
        messagesFlow()
            .flowOn(CoroutineName("Name3"))
            .onEach { present("onEach", it) }
            .flowOn(CoroutineName("Name2"))
            .collect { present("collect", it) }
    }
}

private suspend fun currentName(): String? = coroutineContext[CoroutineName]?.name

private suspend fun launchInDemo() = coroutineScope {
    println("\n=== launchIn ===")
    flowOf("User1", "User2")
        .onStart { println("  Users:") }
        .onEach { println("  $it") }
        .launchIn(this)
}

suspend fun main() {
    lifecycle()
    context()
    launchInDemo()
}
