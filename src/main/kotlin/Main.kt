import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

suspend fun main() {
    flow {
        println("Emitting on thread: ${Thread.currentThread().name}")
        emit(10)
    }
        .map {
            println("Mapping on thread: ${Thread.currentThread().name}")
            it * 2
        }
        .flowOn(Dispatchers.Default) // Changes the context of the flow builder and operators above
        .collect {
            println("Collected $it on thread: ${Thread.currentThread().name}")
        }
}

fun checkingScope() {
    println("18 thread is: ${Thread.currentThread()}")
}
