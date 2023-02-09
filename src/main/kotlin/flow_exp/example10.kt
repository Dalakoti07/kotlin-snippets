package flow_exp

import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.takeUntil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

/**
 * combine everything in 1 go
 */
suspend fun main() {

    flow {
        emit("saurabh")
        for (i in 1..10) {
            emit(i)
        }
        emit("dalakoti")
    }.onEach {
        delay(100)
    }
        .filterIsInstance<Int>()
        .flatMapFirst {
            flowFrom("upstream:$it")
        }
        .collect{
            println(it)
        }

}
