package flow_exp

import com.hoc081098.flowext.flatMapFirst
import kotlinx.coroutines.flow.flowOf

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

/*
fun flowFrom(elem: String) = flowOf(1, 2, 3)
    .onEach { delay(100) }
    .map { "${it}_${elem} " }
 */
suspend fun main() {

    val resultantFlow = flowOf("B", "A", "C")
        .flatMapFirst { flowFrom(it) }
    resultantFlow.collect { println(it) }
    /**
     * 1_B
     * 2_B
     * 3_B
     */

}
