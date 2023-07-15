package coroutines.battleTests

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

suspend fun runCoroutineMind(i: Int){
    delay(1000)
    print("task: $i -> " + Thread.currentThread()+"\n")
}

fun main() = runBlocking(Dispatchers.Default){
    for(i in 1..1000){
        launch {
            runCoroutineMind(i)
        }
    }
}