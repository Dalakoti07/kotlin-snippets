package coroutines.gettingStarted

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main() {
    /**
     * If you run the code above, you can see how the parentJob has children. If you run the same code,
     * removing the context for the second coroutine builder,
     * you can see that the parent-child relationship is not established and the children are not present.
     */

    with(GlobalScope) {
        val parentJob = launch {
            delay(200)
            println("I'm the parent")
            delay(200)
        }
        launch(parentJob) {
            delay(200)
            println("I'm a child")
            delay(200)
        }
        if (parentJob.children.iterator().hasNext()) {
            println("The Job has children ${parentJob.children}")
        } else {
            println("The Job has NO children")
        }
        Thread.sleep(1000)
    }
}