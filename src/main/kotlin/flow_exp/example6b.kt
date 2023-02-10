package flow_exp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

/**
 * https://kt.academy/article/cc-flatmap
 */
suspend fun main() {
    /**
     * The typical use of flatMapMerge is when we need to request data for each element in a flow. For instance,
     * we have a list of categories, and you need to request offers for each of them. You already know that you can do this
     * with the async function. There are two advantages of using a flow with flatMapMerge instead:
     *
     * - we can control the concurrency parameter and decide how many categories we want
     *      to fetch at the same time (to avoid sending hundreds of requests at the same time);
     * - we can return Flow and send the next elements as they arrive (so, on the function-use side,
     *      they can be handled immediately).
     */
    /**
     * Example
     * suspend fun getOffers(
     *     categories: List<Category>
     * ): List<Offer> = coroutineScope {
     *     categories
     *         .map { async { api.requestOffers(it) } }
     *         .flatMap { it.await() }
     * }
     *
     * // A better solution
     * suspend fun getOffers(
     *     categories: List<Category>
     * ): Flow<Offer> = categories
     *     .asFlow()
     *     .flatMapMerge(concurrency = 20) {
     *         suspend { api.requestOffers(it) }.asFlow()
     *         // or flow { emit(api.requestOffers(it)) }
     *     }
     */
    var resultantFlow = flowOf("A", "B", "C")
        .flatMapMerge { flowFrom(it) }
    resultantFlow.collect { println(it) }

    resultantFlow = flowOf("A", "B", "C").flatMapMerge {
        flow {
            emit("Saurabh- $it")
        }
    }
    resultantFlow.collect {
        println(it)
    }

    println("---------------------")
    /**
     * So concat basically means first all values from flowA would be taken and flowB would be concatted at its end no matter what
     */
    resultantFlow = flowOf("A","B").flatMapMerge {
        createSpecialFlow(it)
    }
    resultantFlow.collect {
        println(it)
    }
    println("---------------------")
}