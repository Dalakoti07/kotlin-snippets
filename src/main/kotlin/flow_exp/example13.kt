package flow_exp

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


/*fun main() = runBlocking {
    val flow = flowOf(1, 2, 3).flatMapLatest { number ->
        flow {
            emit("Processing $number")
            delay(1000) // Simulates work
            emit("Done $number")
        }
    }

    flow.collect {
        println(it)
    }
    val p1 = Point(2, 3)
    val p2 = Point(4, 5)
    val p3 = p1 + p2  // Calls `plus` function
    println(p3)  // Output: Point(x=6, y=8)
}

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }
}*/

suspend fun fetchData() {
    coroutineScope {
        launch {
            delay(1000)
            println("Fetching data...")
        }
        launch {
            delay(500)
            println("Processing data...")
        }
    }
    println("All tasks completed!")
}

fun main() = runBlocking {
    fetchData()
}
