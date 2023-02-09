package flow_exp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

/**
 * infinite flow it goes and goes, producer is infinitely producing it
 */
suspend fun main(){

    flow {
        var i = 0
        while(true){
            emit(i)
            i++
            delay(1000)
        }
    }.collect {
        println("it: $it")
    }

}