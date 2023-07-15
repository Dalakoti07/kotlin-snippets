package coroutines.battleTests

import kotlinx.coroutines.*

suspend fun runCoroutine(i: Int){
    delay(1000)
    print("task: $i -> " + Thread.currentThread()+"\n")
}

// so next time dont worry around runblocking, blocking the main thread,
// coroutines FUNDAMENTALLY does not block any thread, and
// so goes for `runBlocking`, `runblocking` just makes sure that whatever
// is launched from its scope get executed before executing
fun main() = runBlocking(Dispatchers.IO){
    for(i in 1..1000){
        launch {
            runCoroutine(i)
        }
    }
}