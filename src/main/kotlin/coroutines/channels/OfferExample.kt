package coroutines.channels

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {

  val fruitArray = arrayOf("Apple", "Banana", "Pear", "Grapes", "Strawberry")

  val kotlinChannel = Channel<String>()

  runBlocking {
    launch {
      for (fruit in fruitArray) {
        val wasSent = kotlinChannel.trySend(fruit)
        if (wasSent.isSuccess) {
          println("Sent: $fruit")
        } else {
          println("$fruit wasn't sent")
        }
      }
      kotlinChannel.close()
    }

    for (fruit in kotlinChannel) {
      println("Received: $fruit")
    }
    println("Done!")
  }
}