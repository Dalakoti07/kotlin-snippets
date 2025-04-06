package prod_consumer

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// FixedBuffer class that uses a Channel with fixed capacity.
class FixedBuffer<T>(capacity: Int = 10) {
    private val channel = Channel<T>(capacity)

    // Produces an item by sending it to the channel.
    // If the channel (buffer) is full, this suspends until space is available.
    suspend fun produce(item: T) {
        channel.send(item)
        // remainingCapacity tells us how many more items can be added before blocking.
        println("Produced: $item")
    }

    // Consumes an item by receiving it from the channel.
    // If the channel is empty, this suspends until an item is available.
    suspend fun consume(): T {
        val item = channel.receive()
        println("Consumed: $item")
        return item
    }
}

fun main() = runBlocking {
    val totalItems = 100
    val buffer = FixedBuffer<Int>(10) // Fixed buffer with capacity 10

    // Launch a coroutine for the producer.
    val producer = launch {
        for (i in 1..totalItems) {
            buffer.produce(i)
            delay(50)  // Simulate a fast production pace
        }
        println("Producer finished producing $totalItems items.")
    }

    // Launch a coroutine for the consumer.
    val consumer = launch {
        repeat(totalItems) {
            buffer.consume()
            delay(150) // Simulate a slower consumption pace
        }
        println("Consumer finished consuming $totalItems items.")
    }

    producer.join()
    consumer.join()
    println("Main coroutine finished.")
}