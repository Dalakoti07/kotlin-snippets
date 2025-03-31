package flow_exp

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Unconfined

fun main(): Unit = runBlocking {
    launch(Dispatchers.Default) {
        println("Running in Default dispatcher on thread: ${Thread.currentThread().name}")

        // Suspend on a delay, then it will be resumed on an Unconfined dispatcher
        launch(Unconfined) {
            println("Running in Unconfined dispatcher on thread: ${Thread.currentThread().name}")
            delay(100)  // Suspension point
            println("Resumed in Unconfined dispatcher on thread: ${Thread.currentThread().name}")
        }

        println("After launch in Default")
    }
}
