import java.sql.DriverManager.println
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

suspend fun main() {

    val scope = CoroutineScope(Dispatchers.IO)

    println("9 thread is: ${Thread.currentThread()}")
    scope.launch {
        println("11 thread is: ${Thread.currentThread()}")
        checkingScope()
    }.join()
    println("14 thread is: ${Thread.currentThread()}")
}

fun checkingScope() {
    println("18 thread is: ${Thread.currentThread()}")
}
