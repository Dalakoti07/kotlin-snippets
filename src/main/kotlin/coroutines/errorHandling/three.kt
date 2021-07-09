package coroutines.errorHandling

import kotlinx.coroutines.*

fun main() {
    /*
    CoroutineExceptionHandler is useful when you want to have a global exception handler shared between coroutines,
    but if you want to handle exceptions for a specific coroutine in a different manner,
    you are required to provide the specific implementation. Let us take a look at how.
     */

    runBlocking {
        // 1
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        // 2
        val job = GlobalScope.launch(exceptionHandler) {
            throw AssertionError("My Custom Assertion Error!")
        }
        // 3
        val deferred = GlobalScope.async(exceptionHandler) {
            // Nothing will be printed,
            // relying on user to call deferred.await()
            throw ArithmeticException()
        }
        // 4
        // This suspends current coroutine until all given jobs are complete.
        joinAll(job, deferred)
    }
}
