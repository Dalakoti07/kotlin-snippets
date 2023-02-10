package flow_exp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

fun flowFrom(elem: String) = flowOf(1, 2, 3)
    .onEach { delay(100) }
    .map { "${it}_${elem} " }

fun createSpecialFlowWhichIsOverlapping(elem: String): Flow<String>{
    val flowDelay: Long = if (elem == "A") 2000 else 1000
    return flowOf(1,2,3,4).onEach {
        delay(flowDelay)
    }.map {
        "${elem}_$it"
    }
}

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
     * Now here flatmap wont do any magic because each flow generated everything at same time
     */
    resultantFlow = flowOf("A", "B", "C").flatMapConcat {
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
     * but flatMap does concat them
     */
    resultantFlow = flowOf("A","B").flatMapConcat {
        createSpecialFlowWhichIsOverlapping(it)
    }
    resultantFlow.collect {
        println(it)
    }
    println("---------------------")

}
