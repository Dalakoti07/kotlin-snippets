package flow_exp

import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.takeUntil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

/**
 * Testing takeUntil
 */
suspend fun main() {

    flow {
        for (i in 1..10) {
            delay(100)
            emit(i)
        }
    }
        // until is inclusive
        // Emits the values emitted by the source Flow until a notifier
        // emits a value or completes.
        .takeUntil(
            flow { emit("3") }.onEach {
                delay(500)
            }
        )
        .collect {
            println(it)
        }

}
