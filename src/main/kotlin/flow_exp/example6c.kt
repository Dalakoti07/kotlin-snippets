package flow_exp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

/**
 * https://kt.academy/article/cc-flatmap
 */
fun flowFrom2(elem: String) = flowOf(1, 2, 3)
    .onEach { delay(1000) }
    .map { "${it}_${elem} " }

suspend fun main() {

    /**
     * so what flat map latest does is
     * It forgets about the previous flow once a new one appears. With every new value,
     * the previous flow processing is forgotten.
     * So, if there is no delay between "A", "B" and "C", then you will only see "1_C", "2_C", and "3_C".
     */
    var resultantFlow = flowOf("A", "B", "C")
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