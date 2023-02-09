package flow_exp

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

fun flowIntProducer(): Flow<Int>{
    return flow {
        var i = 0
        while(true){
            emit(i)
            i++
            delay(1000)
        }
    }
}

/**
 * his means that the producer code is executed each time a terminal operator is called on the flow. In the previous example,
 * having multiple flow collectors causes the data source to fetch the [INT] multiple times on different fixed intervals.
 * To optimize and share a flow when multiple consumers collect at the same time, use the shareIn operator.
 *
 * To see [shareIn] approach see example4
 *
 */
suspend fun main(){

    val res1 = GlobalScope.launch {
        flowIntProducer().collect {
            println("collector 1: $it")
        }
    }

    delay(2000)

    val res2 =GlobalScope.launch {
        flowIntProducer().collect {
            println("collector 2: $it")
        }
    }

    joinAll(res1, res2)
}