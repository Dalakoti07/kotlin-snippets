package coroutines.asyncAwait

import kotlinx.coroutines.*

/*
    But what happens if you cancel the Job from the launch() after its block starts executing? The getUserByIdFromNetwork()
    would still run for three seconds and return a value, even though the parent job is canceled.
    This causes a waste of time and resources in computing, which would be better spent elsewhere. Or at least it did with the initial release.
 */

fun main() {
    val launch = GlobalScope.launch {
        val dataDeferred = getUserByIdFromNetwork(1312)
        println("Not cancelled")
        // do something with the data
        val data = dataDeferred.await()
        println(data)
    }

    Thread.sleep(50)
    launch.cancel()

    while (true) { // stops the program from finishing
    }
}

private fun getUserByIdFromNetwork(
    userId: Int
) =
    GlobalScope.async {
        println("Retrieving user from network")
        delay(3000)
        println("Still in the coroutine")

        User(userId, "Filip", "Babic") // we simulate the network call
    }