package flow_exp

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

suspend fun main() {

    flow {
        emit(1)
        kotlinx.coroutines.delay(1000)
        emit(2)
        kotlinx.coroutines.delay(2000)
        emit(3)

        kotlinx.coroutines.delay(3000)
        /*
        never do context switching use callback-flow for this context switching
        withContext(Dispatchers.IO){
            emit(4)
        }*/
    }.map {
        it * 10
    }.onEach {
        println("on each: $it")
    }
        .map {
            it - 1
        }
        .collect {
            println("final print $it")
        }

}