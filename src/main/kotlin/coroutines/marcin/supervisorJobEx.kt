package coroutines.marcin

import kotlinx.coroutines.*

private val handler = CoroutineExceptionHandler { _, e ->
    println("   [handler] caught: ${e.message}")
}

// ── A ── Regular Job: a failing child cancels the PARENT, which cancels the SIBLING
private suspend fun regularJob() {
    println("A: CoroutineScope(Job())")
    val scope = CoroutineScope(Job() + handler)
    scope.launch {
        delay(100)
        throw Error("child-1 boom")
    }
    scope.launch {
        delay(200)
        println("   child-2 finished")   // never runs
    }
    delay(300)
    println("   scope job active? ${scope.coroutineContext.job.isActive}")
}

// ── B ── SupervisorJob: the failure stops ONLY the failing child
private suspend fun supervisorJob() {
    println("B: CoroutineScope(SupervisorJob())")
    val scope = CoroutineScope(SupervisorJob() + handler)
    scope.launch {
        delay(100)
        throw Error("child-1 boom")
    }
    scope.launch {
        delay(200)
        println("   child-2 finished")   // runs
    }
    delay(300)
    println("   scope job active? ${scope.coroutineContext.job.isActive}")
}

// ── C ── supervisorScope: same isolation, but structured (it waits for children)
private suspend fun supervisorScopeEx() {
    println("C: supervisorScope { }")
    supervisorScope {
        launch(handler) {
            delay(100)
            throw Error("child-1 boom")
        }
        launch {
            delay(200)
            println("   child-2 finished")   // runs
        }
    }
    println("   after supervisorScope")     // runs
}

// ── D ── The exception is NOT thrown in a builder but in the scope body itself
private suspend fun thrownInScopeBody() {
    println("D: throw directly inside supervisorScope body")
    try {
        supervisorScope {
            launch {
                delay(200)
                println("   child never gets here")
            }
            delay(100)
            throw Error("body boom")        // not a child -> propagates out
        }
    } catch (e: Error) {
        println("   [caught by caller] ${e.message}")
    }
    delay(300)
}

suspend fun main() {
    regularJob();        println()
    supervisorJob();     println()
    supervisorScopeEx(); println()
    thrownInScopeBody()
}
