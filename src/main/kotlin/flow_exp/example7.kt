package flow_exp

import com.hoc081098.flowext.flatMapFirst
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/*actionSharedFlow
.filterIsInstance<GithubSearchAction.LoadNextPage>()
.flatMapFirst {
    flowFromSuspend { stateFlow.value }
        .filter { it.canLoadNextPage }
        .flatMapConcat {
            executeSearchRepoItemsUseCase(
                term = it.term,
                nextPage = it.page + 1u
            )
        }
        .takeUntil(
            actionSharedFlow
                .filterIsInstance<SideEffectAction.TextChanged>()
        )
}*/

/**
 * Exploring flatmapFirst
 */

fun flowForMapFirst(elem: String) = flowOf(1, 2, 3)
    .onEach {
        delay((it * 100).toLong())
    }
    .map { "${it}_${elem} " }

suspend fun main() {

    var resultantFlow = flowOf("A", "B", "C")
        .flatMapFirst { flowFrom(it) }
    resultantFlow.collect { println(it) }
    /**
     * 1_A
     * 2_A
     * 3_A
     */

    println("........................")

    resultantFlow = flowOf("A", "B", "C")
        .flatMapFirst {
            flow {
                emit("E")
            }
        }
    resultantFlow.collect { println(it) }

    println("........................")

    /**
     * This shows that only first Element's transformed element
     * would make to next round
     */
    resultantFlow = flowOf("A", "B", "C")
        .onEach {
            delay(1000)
        }
        .flatMapFirst {
            createSpecialFlowWhichIsOverlapping(it)
        }
    resultantFlow.collect { println(it) }

}
