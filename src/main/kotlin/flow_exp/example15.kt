package flow_exp

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() = runBlocking {
    val flow1 = flowOf(1, 2, 3).onEach { delay(200) }
    val flow2 = flowOf("A", "B", "C").onEach { delay(300) }

    flow1.combine(flow2) { a, b -> "$a$b" }
        .collect { println(it) }
}
