package features

//without cross line
/*inline fun doSomething(task: () -> Unit) {
    task()  // Calling the lambda
    println("After lambda execution")
}

fun main() {
    doSomething {
        println("Inside Lambda")
        return  // Non-local return (exits `main` function)
    }
    println("This will never be printed")
}*/

inline fun doSomething(crossinline task: () -> Unit) {
    val runnable = Runnable { task() }  // Using the lambda inside another function
    runnable.run()
}

fun main() {
    doSomething {
        println("Inside Lambda")
//         return  // ❌ ERROR: Cannot use non-local return here
    }
    println("After Lambda Execution")
}

