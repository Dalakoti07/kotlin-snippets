package flow_exp

import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.takeUntil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow

/**
 * Testing flowFromSuspend
 */
suspend fun returnInt(value: Int): Int {
    delay(1000)
    return value
}

suspend fun main() {

    flowFromSuspend {
        returnInt(2)
    }.filter {
        it == 2
    }.flatMapConcat {
        flow {
            emit("string: $it")
        }
    }
        .collect {
            println(it)
        }

}
