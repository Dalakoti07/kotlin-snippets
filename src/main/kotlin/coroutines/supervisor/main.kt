package coroutines.supervisor

import kotlinx.coroutines.*

// if SupervisorJob is removed then program would exit early
val scope = CoroutineScope(Dispatchers.IO +  SupervisorJob())

suspend fun main() {
    printSomething2()
    delay(100)
    printSomething()
}

fun printSomething2(){
    scope.launch {
        println("will give exception")
        throw Exception("wrong happened")
    }
}

fun printSomething(){
    scope.launch {
        println("works?")
    }
}
