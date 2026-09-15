package coroutines.marcin

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

suspend fun main(): Unit = coroutineScope{
    val mStateFlow = MutableStateFlow("x")
    launch {
        for(c in 'A'..'E'){
            delay(50)
            mStateFlow.value = c.toString()
        }
    }
    mStateFlow.collect {
        delay(100)
        println(it)
    }
}