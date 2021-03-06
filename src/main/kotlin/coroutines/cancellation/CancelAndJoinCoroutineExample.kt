package coroutines.cancellation

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
  val job = launch {
    repeat(1000) { i ->
      println("$i. Crunching numbers [Beep.Boop.Beep]...")
      delay(1000L)
    }
  }
  delay(2300L) // delay a bit
  println("main: I am tired of waiting!")
  // cancels the job and waits for job's completion
  job.cancelAndJoin()
  println("main: Now I can quit.")
}