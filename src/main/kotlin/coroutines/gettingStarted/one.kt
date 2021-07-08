package coroutines.gettingStarted

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {
    //first example
  (1..10000).forEach {
    val threadName = Thread.currentThread().name
    // counting would be printed in any order
    GlobalScope.launch { println("$it printed on thread $threadName") }
  }
  Thread.sleep(1000)
}