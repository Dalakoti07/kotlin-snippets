package coroutines.marcin

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.zip

/** Flow processing chapter. */

private var t0 = System.currentTimeMillis()
private fun log(m: String) = println("  %4d ms  %s".format(System.currentTimeMillis() - t0, m))
private fun reset() { t0 = System.currentTimeMillis() }

private data class FUser(val id: Int, val name: String) {
    override fun toString(): String = "[$id] $name"
}

suspend fun main() {
    println("\n=== merge: no waiting between sources ===")
    reset()
    val ints: Flow<Number> = flowOf(1, 2, 3).onEach { delay(400) }
    val doubles: Flow<Number> = flowOf(0.1, 0.2, 0.3)
    val together: Flow<Number> = merge(ints, doubles)
    together.collect { log("$it") }

    println("\n=== zip: strict pairing, ends with the shorter flow ===")
    reset()
    flowOf("A", "B", "C").onEach { delay(160) }
        .zip(flowOf(1, 2, 3, 4).onEach { delay(400) }) { f1, f2 -> "${f1}_${f2}" }
        .collect { log(it) }

    println("\n=== combine: newest with newest, ends with the longer flow ===")
    reset()
    flowOf("A", "B", "C").onEach { delay(160) }
        .combine(flowOf(1, 2, 3, 4).onEach { delay(400) }) { f1, f2 -> "${f1}_${f2}" }
        .collect { log(it) }

    println("\n=== scan emits intermediate accumulators ===")
    println("  list.scan  -> ${listOf(1, 2, 3, 4).scan(0) { acc, i -> acc + i }}")
    reset()
    flowOf(1, 2, 3, 4).onEach { delay(200) }.scan(0) { acc, v -> acc + v }.collect { log("$it") }
    println("  fold (terminal) -> ${flowOf(1, 2, 3, 4).fold(0) { acc, i -> acc + i }}")

    println("\n=== retry ===")
    print("  ")
    try {
        flow {
            emit(1); emit(2); error("E"); emit(3)
        }.retry(3) { print(it.message); true }
            .collect { print(it) }
    } catch (e: IllegalStateException) {
        print("(exception thrown)")
    }
    println()

    println("\n=== distinctUntilChanged ===")
    print("  flowOf(1,2,2,3,2,1,1,3) -> ")
    flowOf(1, 2, 2, 3, 2, 1, 1, 3).distinctUntilChanged().collect { print(it) }
    println()
    val users = flowOf(FUser(1, "Alex"), FUser(1, "Bob"), FUser(2, "Bob"), FUser(2, "Celine"))
    println("  by id    -> ${users.distinctUntilChangedBy { it.id }.toList()}")
    println("  by name  -> ${users.distinctUntilChangedBy { it.name }.toList()}")
    println("  custom   -> ${users.distinctUntilChanged { p, n -> p.id == n.id || p.name == n.name }.toList()}")
}
