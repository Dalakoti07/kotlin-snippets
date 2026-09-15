package coroutines.marcin

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

fun getSequence(): Sequence<String> = sequence{
    repeat(3){
        Thread.sleep(1000)
        yield("user $it")
    }
}

fun userFlow(): Flow<String> = flow {
    repeat(3){
        delay(1000)
        val ctx = currentCoroutineContext()
        val name = ctx[CoroutineName]?.name
        emit("User $it in $name")
    }
}

suspend fun main(){
   val users = userFlow()
    withContext(CoroutineName("name")){
        val job = launch {
            users.collect {
                println(it)
            }
        }
        launch {
            delay(2100)
            println("I got enough")
            job.cancel()
        }
    }

}