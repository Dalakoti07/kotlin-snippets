package coroutines.battleTests

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

// will print this runCoroutineMind sequentially
/*fun main() = runBlocking {
    val jobs = (1..1000).map { i ->
        async {
            runCoroutineMind(i)
        }
    }
    jobs.awaitAll()  // Ensures all coroutines finish before proceeding
    Unit
}*/
