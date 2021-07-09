package coroutines.broadcast

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCoroutinesApi::class)
fun main() {

  val fruitArray = arrayOf("Apple", "Banana", "Pear", "Grapes",
    "Strawberry")
  // 1
  val kotlinChannel = BroadcastChannel<String>(3)

  runBlocking {

    // 2
    kotlinChannel.apply {
      send(fruitArray[0])
      send(fruitArray[1])
      send(fruitArray[2])
    }

    //3  Consumers
    GlobalScope.launch {
      // 4
      kotlinChannel.openSubscription().let { channel ->
        // 5
        for (value in channel) {
          println("Consumer 1: $value")
        }
        // 6
      }
    }
    GlobalScope.launch {
      kotlinChannel.openSubscription().let { channel ->
        for (value in channel) {
          println("Consumer 2: $value")
        }
      }
    }

    // 7
    kotlinChannel.apply {
      send(fruitArray[3])
      send(fruitArray[4])
    }

    // 8
    println("Press a key to exit...")
    readLine()

    // 9
    kotlinChannel.close()
  }
}
