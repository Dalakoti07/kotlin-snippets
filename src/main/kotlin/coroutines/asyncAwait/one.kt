package coroutines.asyncAwait

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

fun main() {
    val userId = 992

    GlobalScope.launch {
        val userData = getUserByIdFromNetwork(userId)
        println("line 12 is executed ...... ")
        println(userData.await())
        println("line 14 is executed when above await value returns ......")
    }
    println("line 16 main thread is not blocked ")
    Thread.sleep(5000)
}

private fun getUserByIdFromNetwork(userId: Int) = GlobalScope.async {
    Thread.sleep(3000)

    User(userId, "Filip", "Babic")
}
