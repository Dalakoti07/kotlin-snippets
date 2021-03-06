package coroutines.errorHandling

import kotlinx.coroutines.*

fun main() = runBlocking {

    /**
     * In this example if child fails then parent also fails
     */

    // Global Exception Handler
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught $exception with suppressed " +

                // Get the suppressed exception
                "${exception.suppressed?.contentToString()}")
    }

    // Parent Job
    val parentJob = GlobalScope.launch(handler) {
        // Child Job 1
        launch {
            try {
                delay(Long.MAX_VALUE)
            } catch (e: Exception) {
                println("${e.javaClass.simpleName} in Child Job 1")
            } finally {
                throw ArithmeticException()
            }
        }

        // Child Job 2
        launch {
            delay(2000)
            throw IllegalStateException()
        }

        // Delaying the parentJob
        delay(Long.MAX_VALUE)
    }
    // Wait until parentJob completes
    parentJob.join()
}
