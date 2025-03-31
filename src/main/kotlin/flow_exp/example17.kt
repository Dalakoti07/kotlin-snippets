package flow_exp
/*

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() = runBlocking {
    // Example flow that emits integers and performs some background computation
    flow {
        println("Emitting on thread: ${Thread.currentThread().name}")
        emit(1)  // Emitting values on the default thread (main or IO initially)
        delay(1000)
        println("Last Emitting on thread: ${Thread.currentThread().name}")
        emit(2)
    }
        .flowOn(Dispatchers.IO) // Perform the flow work on the IO dispatcher (background thread)
        .collect { value ->
            println("Received: $value on thread: ${Thread.currentThread().name}")
        }
}
*/

/*
Biryani		70
Noodles	30 -> burger
Sandwich	40 -> burger
Burger		10
Cake		50
Manchurian	30
*/

// op
/*
Noodles
Burger
Burger
NA
Manchurian
NA
*/
fun main(args: Array<String>) {
    val dishes = listOf("Biryani", "Noodles","Sandwitch","Burger","Cake","Machurian")
    val timeIs = listOf(70,30,40,10,50,30);

    // brute force is N^2

    // Solution

    // stack [(3, 10), (2,40), (1, 30)]
    val stackIs = mutableListOf<Pair<Int, Int>>()
    val n = dishes.size;
    stackIs.add(Pair(n - 1, timeIs[n-1]))
    val outputArray = mutableListOf("NA")
    // traverse the array from reverse order
    var i = n-2
    while(i>=0){
        val currentTime = timeIs[i]
        while(!stackIs.isEmpty() && stackIs.last().second > currentTime){
            stackIs.removeLast()
        }
        if(stackIs.isEmpty()){
            outputArray.add("NA")
        }else{
            val topItem = stackIs.last()
            outputArray.add(dishes[topItem.first])
            stackIs.add(Pair(i, currentTime))
        }
        i--;
    }
    println(outputArray)
}
