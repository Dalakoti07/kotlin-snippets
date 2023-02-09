package flow_exp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

fun flowFrom(elem: String) = flowOf(1, 2, 3)
    .onEach { delay(100) }
    .map { "${it}_${elem} " }

fun flowFrom2(elem: String) = flowOf(1, 2, 3)
    .onEach { delay(1000) }
    .map { "${it}_${elem} " }

/**
 * https://kt.academy/article/cc-flatmap
 */

suspend fun main() {
    // given a list of string and each string can generate a flow of strings
    // flatmap flatten the string array in one level
    /**
     * [flatMapConcat] is same as [flatMap] (this is deprecated)
     */
    var resultantFlow = flowOf("A", "B", "C")
        .flatMapConcat { flowFrom(it) }
    resultantFlow.collect { println(it) }


    println("---------------------")
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
    resultantFlow = flowOf("A", "B", "C")
        .flatMapMerge { flowFrom(it) }
    resultantFlow.collect { println(it) }


    println("---------------------")
    /**
     * so what flat map latest does is
     * It forgets about the previous flow once a new one appears. With every new value,
     * the previous flow processing is forgotten.
     * So, if there is no delay between "A", "B" and "C", then you will only see "1_C", "2_C", and "3_C".
     */
    resultantFlow = flowOf("A", "B", "C")
        .flatMapLatest { flowFrom(it) }
    resultantFlow.collect { println(it) }

    println("......................")

    /**
     * This is different example now with delays
     * It gets more interesting when the elements from the initial flow are delayed.
     * What happens in the example below is that (after 1.2 sec) "A" starts its flow, which was created using flowFrom.
     * This flow produces an element "1_A" in 1 second, but 200 ms later "B" appears and this previous flow is closed and forgotten.
     * "B" flow managed to produce "1_B" when "C" appeared and started producing its flow.
     * This one will finally produce elements "1_C", "2_C", and "3_C", with a 1-second delay in between.
     */
    resultantFlow = flowOf("A", "B", "C")
        .onEach { delay(1200) }
        .flatMapLatest { flowFrom2(it) }

    resultantFlow.collect { println(it) }

}