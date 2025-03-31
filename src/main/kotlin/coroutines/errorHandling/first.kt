package coroutines.errorHandling

import core.regex.StyledTextModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val asyncJob = GlobalScope.launch {
        println("1. Exception created via launch coroutine")

        // Will be printed to the console by
        // Thread.defaultUncaughtExceptionHandler
        throw IndexOutOfBoundsException()
    }

    asyncJob.join()
    println("2. Joined failed job")

    val deferred = GlobalScope.async {
        println("3. Exception created via async coroutine")

        // Nothing is printed, relying on user to call await
        throw ArithmeticException()
    }

    val string = StyledTextModel.StyledTextIndices(startIndex = 12)
    val result = string.run {
        "Result is $startIndex"
    }
    println("result is $result")
    try {
        deferred.await()
        println("4. Unreachable, this statement is never executed")
    } catch (e: Exception) {
        println("5. Caught ${e.javaClass.simpleName}")
    }
}
