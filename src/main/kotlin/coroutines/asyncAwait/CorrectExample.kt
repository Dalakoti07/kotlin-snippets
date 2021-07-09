package coroutines.asyncAwait

import kotlinx.coroutines.*

fun main() {
    val launch = GlobalScope.launch {
        val dataDeferred = getUserByIdFromNetwork(1312,this)
        if(isActive){
            println("Not cancelled")
            // do something with the data
            val data = dataDeferred.await()
            println(data)
        }else{
            println("cancelled")
        }
    }

    Thread.sleep(50)
    launch.cancel()

    while (true) { // stops the program from finishing
    }
}

private suspend fun getUserByIdFromNetwork(
    userId: Int,
    parentScope: CoroutineScope
) =
    parentScope.async {
        if (!isActive) {
            return@async User(0, "", "")
        }
        println("Retrieving user from network")
        delay(3000)
        println("Still in the coroutine")

        User(userId, "Filip", "Babic") // we simulate the network call
    }
