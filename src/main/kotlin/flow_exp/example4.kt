package flow_exp

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * The [shareIn] approach (optimisation such that intensive work is not repeated)
 *
 * The shareIn operator is useful in situations when there is a cold flow that is expensive to create and/or to maintain,
 * but there are multiple subscribers that need to collect its values. For example,
 * consider a flow of messages coming from a backend over the expensive network connection,
 * taking a lot of time to establish. Conceptually, it might be implemented like this:
 *
 */
suspend fun main() {
    val scope =
        CoroutineScope(Dispatchers.IO + Job())

    val sharedFlow = flowIntProducer().shareIn(
        scope,
        started = SharingStarted.WhileSubscribed(),
        replay = 0, // denotes how many last values need to be re-emitted when a new subscriber subscribes
        // so if you do replay = 2, then collector 2 will get 0 and 1 both which were emitted even before collector_2 subscribed
    )

    val job1 = scope.launch {
        sharedFlow
            .collect {
                println("collector 1: $it")
            }
    }

    delay(2000)

    val job2 =scope.launch {
        sharedFlow
            .collect {
                println("collector 2: $it")
            }
    }

    joinAll(job1, job2)
}