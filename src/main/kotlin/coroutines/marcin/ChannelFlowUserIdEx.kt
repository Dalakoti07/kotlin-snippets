package coroutines.marcin

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

/**
 * "Request something for each id" -- the shape the book uses to motivate channelFlow.
 * Which parts of it actually require channelFlow, and which do not.
 *
 *   ./gradlew runMain -PmainClass=coroutines.marcin.ChannelFlowUserIdExKt --args="1"
 */

private var t0 = System.currentTimeMillis()
private fun log(msg: String) = println("  %5d ms  %s".format(System.currentTimeMillis() - t0, msg))

private data class User(val id: Int, val name: String)

private object Api {
    // id -> how slow that particular request is
    private val latency = mapOf(1 to 300L, 2 to 100L, 3 to 200L, 4 to 50L)

    suspend fun fetchUser(id: Int): User {
        delay(latency.getValue(id))
        return User(id, "user-$id")
    }

    /** Page 0,1,2 have users; page 3 is empty and ends the stream. */
    suspend fun takePage(page: Int): List<User> {
        delay(300)                                  // network
        log("   API: page $page fetched")
        return if (page < 3) List(2) { User(page * 2 + it, "user-${page * 2 + it}") } else emptyList()
    }
}

// ---------------------------------------------------- 1. per-id requests

private suspend fun exp1() {
    val ids = listOf(1, 2, 3, 4)

    println("\n=== A. flow {} -- one id at a time ===")
    t0 = System.currentTimeMillis()
    val sequential: Flow<User> = flow {
        for (id in ids) emit(Api.fetchUser(id))
    }
    val seq = measureTimeMillis { sequential.collect { log("got ${it.name}") } }
    println("  total ${seq}ms = sum of every latency, results in ID order")

    println("\n=== B. channelFlow -- all ids in flight, emit as each returns ===")
    t0 = System.currentTimeMillis()
    val concurrent: Flow<User> = channelFlow {
        for (id in ids) {
            launch { send(Api.fetchUser(id)) }      // emit from a CHILD coroutine
        }
    }
    val con = measureTimeMillis { concurrent.collect { log("got ${it.name}") } }
    println("  total ${con}ms = slowest single latency, results in COMPLETION order")

    println("\n=== C. the same result with flatMapMerge, no channelFlow ===")
    t0 = System.currentTimeMillis()
    @Suppress("OPT_IN_USAGE")
    val merged: Flow<User> = ids.asFlow()
        .flatMapMerge(concurrency = 20) { id -> flow { emit(Api.fetchUser(id)) } }
    val mer = measureTimeMillis { merged.collect { log("got ${it.name}") } }
    println("  total ${mer}ms -- same concurrency, and you get a concurrency cap for free")
    println("\n  => for THIS shape, channelFlow is not the only answer.")
    println("     flatMapMerge is itself built on channelFlow -- it just hides it.")
}

// ---------------------------------------------------- 2. paged fetching

private suspend fun exp2() {
    println("\n=== D. paged fetch, flow {} -- next page waits for the consumer ===")
    t0 = System.currentTimeMillis()
    val paged: Flow<User> = flow {
        var page = 0
        do {
            val users = Api.takePage(page++)
            users.forEach { emit(it) }
        } while (users.isNotEmpty())
    }
    val a = measureTimeMillis { paged.collect { delay(200); log("processed ${it.name}") } }
    println("  total ${a}ms -- fetch and processing strictly alternate")

    println("\n=== E. same producer via channelFlow -- prefetches while consumer works ===")
    t0 = System.currentTimeMillis()
    val pagedCf: Flow<User> = channelFlow {
        var page = 0
        do {
            val users = Api.takePage(page++)
            users.forEach { send(it) }
        } while (users.isNotEmpty())
    }
    val b = measureTimeMillis { pagedCf.collect { delay(200); log("processed ${it.name}") } }
    println("  total ${b}ms")

    println("\n=== F. ...but plain flow {} + .buffer() does the same thing ===")
    t0 = System.currentTimeMillis()
    val c = measureTimeMillis { paged.buffer().collect { delay(200); log("processed ${it.name}") } }
    println("  total ${c}ms")
    println("\n  => D vs E is a BUFFERING difference, not a channelFlow-only capability.")
    println("     Only B (emitting from launched children) is impossible with flow {}.")
}

suspend fun main(args: Array<String>) {
    when (args.firstOrNull()) {
        "1" -> exp1()
        "2" -> exp2()
        else -> { exp1(); exp2() }
    }
}
