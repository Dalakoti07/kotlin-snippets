package coroutines.marcin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Random

suspend fun main() = coroutineScope{
    repeat(1000){
        launch(Dispatchers.IO) {
            List(100){kotlin.random.Random.nextLong()}.maxOrNull()
            val threadName = Thread.currentThread().name
            println("running on thread $threadName")
        }
    }
}