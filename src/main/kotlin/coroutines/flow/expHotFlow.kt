package coroutines.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

val sharedFlow = MutableSharedFlow<Int>()

suspend fun main() {
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    coroutineScope.launch {
        (1..1_00).forEach {
            delay(500)
            sharedFlow.emit(it)
        }
    }
    val job1 = coroutineScope.launch {
        sharedFlow.sample(1000)
            .collectLatest {
                println("early bird -> $it")
            }
    }
    delay(10_000)
    val job2 = coroutineScope.launch {
        sharedFlow.sample(1000).collectLatest {
            println("second bird -> $it")
        }
    }

    job1.join()
    job2.join()
}
