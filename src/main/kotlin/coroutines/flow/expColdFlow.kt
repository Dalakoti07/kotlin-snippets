package coroutines.flow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.callbackFlow

val continuousFlow = callbackFlow {
    (1..1_000_000).forEach {
        delay(500)
        trySend(it)
    }
}

suspend fun main(){

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    val job1 = coroutineScope.launch {
        continuousFlow.collect {
            println("early bird -> $it")
        }
    }
    delay(10_000)
    val job2 = coroutineScope.launch {
        continuousFlow.collect {
            println("second bird -> $it")
        }
    }

    job1.join()
    job2.join()
}
