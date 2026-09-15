package flow_exp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlin.system.measureTimeMillis

/**
 * map vs flatMap on Flow.
 *
 *   ./gradlew runMain -PmainClass=flow_exp.Example6dKt
 *
 * map     : (T) -> R          1 element in, 1 element out
 * flatMap*: (T) -> Flow<R>    1 element in, N elements out, then flattened
 *
 * There is no plain `flatMap` on Flow. Elements are spread over time, so
 * "should flow B wait for flow A?" has no single right answer -- hence three
 * functions instead of one: flatMapConcat, flatMapMerge, flatMapLatest.
 */

private var t0 = System.currentTimeMillis()
private fun log(msg: String) = println("  %4d ms  %s".format(System.currentTimeMillis() - t0, msg))
private fun reset() { t0 = System.currentTimeMillis() }

/** One element in -> a flow of 3 elements out, 300ms apart. */
private fun sub(elem: String): Flow<String> = flowOf(1, 2, 3)
    .onEach { delay(300) }
    .map { "${it}_$elem" }

// ------------------------------------------------------ 1. the type difference

private suspend fun typeDifference() {
    println("\n=== 1. The types don't even match ===")

    // map keeps the shape: one element in, one element out
    val mapped: Flow<Flow<String>> = flowOf("A", "B").map { sub(it) }
    println("  map    -> Flow<Flow<String>>")
    mapped.collect { inner -> println("    element is a Flow object: $inner") }

    // flatMap* flattens that extra layer away
    @Suppress("OPT_IN_USAGE")
    val flattened: Flow<String> = flowOf("A", "B").flatMapConcat { sub(it) }
    println("  flatMapConcat -> Flow<String>")
    println("    elements: ${flattened.toList()}")

    // the list analogy the book uses
    val departments = listOf(listOf("emp1", "emp2"), listOf("emp3"))
    println("\n  list .map      -> ${departments.map { it }}")
    println("  list .flatMap  -> ${departments.flatMap { it }}")
}

// ------------------------------------------------------ 2. 1->1 vs 1->N

private suspend fun cardinality() {
    println("\n=== 2. Cardinality: 3 in -> 3 out, vs 3 in -> 9 out ===")

    val viaMap = flowOf("A", "B", "C").map { "mapped_$it" }.toList()
    println("  map           : ${viaMap.size} elements -> $viaMap")

    @Suppress("OPT_IN_USAGE")
    val viaFlatMap = flowOf("A", "B", "C").flatMapConcat { sub(it) }.toList()
    println("  flatMapConcat : ${viaFlatMap.size} elements -> $viaFlatMap")
}

// ------------------------------------------------------ 3. timing of the three

@Suppress("OPT_IN_USAGE")
private suspend fun timing() {
    println("\n=== 3. Why there are three flatMaps and not one ===")

    println("\n  flatMapConcat -- one inner flow at a time, order guaranteed")
    reset()
    val c = measureTimeMillis {
        flowOf("A", "B", "C").flatMapConcat { sub(it) }.collect { log(it) }
    }
    println("  took ${c}ms")

    println("\n  flatMapMerge -- all inner flows at once, order by arrival")
    reset()
    val m = measureTimeMillis {
        flowOf("A", "B", "C").flatMapMerge { sub(it) }.collect { log(it) }
    }
    println("  took ${m}ms")

    println("\n  flatMapMerge(concurrency = 2) -- at most 2 inner flows live")
    reset()
    val m2 = measureTimeMillis {
        flowOf("A", "B", "C").flatMapMerge(concurrency = 2) { sub(it) }.collect { log(it) }
    }
    println("  took ${m2}ms")

    println("\n  flatMapLatest -- a new element cancels the previous inner flow")
    reset()
    val l = measureTimeMillis {
        flowOf("A", "B", "C").flatMapLatest { sub(it) }.collect { log(it) }
    }
    println("  took ${l}ms  (only C survived)")
}

// ------------------------------------------------------ 4. the real use case

private class Api {
    suspend fun requestOffers(category: String): List<String> {
        delay(500)
        return listOf("$category-offer1", "$category-offer2")
    }
}

@Suppress("OPT_IN_USAGE")
private suspend fun realUseCase() {
    println("\n=== 4. One request per element ===")
    val api = Api()
    val categories = listOf("books", "games", "toys", "tools")

    println("\n  map -- Flow<List<Offer>>, sequential, caller must flatten")
    reset()
    val a = measureTimeMillis {
        categories.asFlow()
            .map { api.requestOffers(it) }
            .collect { log("got a List of ${it.size}: $it") }
    }
    println("  took ${a}ms")

    println("\n  flatMapMerge -- Flow<Offer>, concurrent, offers arrive as they land")
    reset()
    val b = measureTimeMillis {
        categories.asFlow()
            .flatMapMerge(concurrency = 20) { flow { api.requestOffers(it).forEach { o -> emit(o) } } }
            .collect { log("got an Offer: $it") }
    }
    println("  took ${b}ms")
}

// ------------------------------------------------------ 5. flatMap == map + flatten

@Suppress("OPT_IN_USAGE")
private suspend fun equivalence() {
    println("\n=== 5. flatMapMerge is literally map + flattenMerge ===")
    reset()
    val viaFlatMap = flowOf("A", "B").flatMapMerge { sub(it) }.toList()
    val viaMapFlatten = flowOf("A", "B").map { sub(it) }.flattenMerge().toList()
    println("  flatMapMerge{}          -> $viaFlatMap")
    println("  map{}.flattenMerge()    -> $viaMapFlatten")
    println("  same? ${viaFlatMap.toSet() == viaMapFlatten.toSet()}")
    println("  => flatMap = map (which nests) + flatten (which unnests)")
}

suspend fun main() {
    typeDifference()
    cardinality()
    timing()
    realUseCase()
    equivalence()
}
