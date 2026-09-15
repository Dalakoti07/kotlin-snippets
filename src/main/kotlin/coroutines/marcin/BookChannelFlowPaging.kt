package coroutines.marcin

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

/**
 * The book's own example: "Flow building" chapter, channelFlow section.
 * Looking for User3 while pages are fetched -- lazily (flow) vs eagerly (channelFlow).
 *
 *   ./gradlew runMain -PmainClass=coroutines.marcin.BookChannelFlowPagingKt
 */

private data class BookUser(val name: String)

private class FakeUserApi {
    private val users = List(20) { BookUser("User$it") }
    private val pageSize: Int = 3
    val calls = AtomicInteger()

    suspend fun takePage(pageNumber: Int): List<BookUser> {
        calls.incrementAndGet()
        delay(1000)
        return users.drop(pageSize * pageNumber).take(pageSize)
    }
}

private var t0 = System.currentTimeMillis()
private fun log(msg: String) = println("  %5d ms  %s".format(System.currentTimeMillis() - t0, msg))

// --- the book's three shapes -------------------------------------------------

private fun lazyFlow(api: FakeUserApi): Flow<BookUser> = flow {
    var page = 0
    do {
        log("Fetching page $page")
        val users = api.takePage(page++)
        emitAll(users.asFlow())
    } while (users.isNotEmpty())
}

private fun eagerChannelFlow(api: FakeUserApi): Flow<BookUser> = channelFlow {
    var page = 0
    do {
        log("Fetching page $page")
        val users = api.takePage(page++)
        users.forEach { send(it) }
    } while (users.isNotEmpty())
}

private suspend fun findUser3(label: String, api: FakeUserApi, source: Flow<BookUser>) {
    println("\n=== $label ===")
    t0 = System.currentTimeMillis()
    val ms = measureTimeMillis {
        val user = source.first {
            log("Checking ${it.name}")
            delay(1000)
            it.name == "User3"
        }
        log("FOUND $user")
    }
    println("  total ${ms}ms, network calls = ${api.calls.get()}")
}

suspend fun main() {
    val a = FakeUserApi()
    findUser3("A. flow {}  -- book's lazy version", a, lazyFlow(a))

    val b = FakeUserApi()
    findUser3("B. channelFlow  -- book's eager version", b, eagerChannelFlow(b))

    val c = FakeUserApi()
    findUser3("C. flow {}.buffer()  -- NOT in the book", c, lazyFlow(c).buffer())

    println("\n  A = fewest network calls, slowest")
    println("  B = fastest, most network calls")
    println("  C = ?")
}
