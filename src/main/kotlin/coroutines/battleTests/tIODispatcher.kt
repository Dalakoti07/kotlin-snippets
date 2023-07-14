package coroutines.battleTests

import kotlinx.coroutines.*

fun performCPUIntensiveTask(id: Int) {
    var i = 0
    // Simulate a CPU-intensive task
    repeat(1_000_000) {
        // Perform some computation
        i++
    }
    print("Done for taskId $id: $i\n")
}

fun doStuff() = runBlocking{
    val numberOfTasks = 100 // Total number of tasks to simulate
    repeat(numberOfTasks) { taskId ->
        launch(Dispatchers.IO) {
            println("Task $taskId started on thread ${Thread.currentThread().name}")
            performCPUIntensiveTask(taskId)
            println("Task $taskId completed")
        }
    }
}

fun main() = runBlocking{
    val startTime = System.currentTimeMillis()
    doStuff()
    val elapsedTime = System.currentTimeMillis() - startTime
    println("Elapsed time: $elapsedTime milliseconds")
}
