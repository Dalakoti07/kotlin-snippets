package coroutines.gettingStarted

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main() {
    //second example
    GlobalScope.launch {
      println("Hello coroutine!")
      delay(1000)
      println("Right back at ya!")
    }

    Thread.sleep(2000)
}