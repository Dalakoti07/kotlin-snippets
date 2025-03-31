package flow_exp

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/*
fun main() = runBlocking {
    flow {
        repeat(5) {
            emit(it)
            println("Emitted: $it")
        }
    }
        .collect {
            delay(1000) // Simulate slow consumer
            println("Collected: $it")
        }
}
*/


fun main() = runBlocking {
    flow {
        repeat(5) {
            emit(it)
            println("Emitted: $it")
        }
    }
        .buffer() // Introduce a buffer
        .collect {
            delay(1000) // Simulate slow consumer
            println("Collected: $it")
        }
}
