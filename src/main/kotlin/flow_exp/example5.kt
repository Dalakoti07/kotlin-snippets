package flow_exp

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

suspend fun main(){

    val stateFlow = MutableStateFlow("initial")

    val scope =
        CoroutineScope(Dispatchers.IO + Job())

    scope.launch {
        stateFlow.collect {
            println("flow: $it")
        }
    }

    for(i in listOf("saurabh","abhishek","anuj","aman")){
        delay(1000)
        stateFlow.value = i
    }

}