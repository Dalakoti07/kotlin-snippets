package coroutines.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

private val stateFlow1 = MutableStateFlow(1)
private val stateFlow2 = MutableStateFlow(10)
private val stateFlow3 = MutableStateFlow(100)

suspend fun main() {
    val scope1 = CoroutineScope(Dispatchers.Default)

    scope1.launch {
        delay(1000)
        stateFlow1.emit(2)
        delay(1000)
        stateFlow2.emit(20)
        delay(1000)
        stateFlow3.emit(200)
        exitProcess(0)
    }

    combine(
        stateFlow1,
        stateFlow2,
        stateFlow3
    ) { one, two, three ->
        one + two + three
    }.collect {
        println("result is -> $it")
    }
}