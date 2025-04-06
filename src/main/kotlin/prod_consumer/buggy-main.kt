package prod_consumer

import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

// Shared buffer without synchronization
class SharedBuffer {
    private val items = mutableListOf<Int>()

    // No synchronization around items.add(...)
    fun produce(item: Int) {
        items.add(item)
    }

    // No synchronization around items.removeAt(...)
    fun consume(): Int? {
        return if (items.isNotEmpty()) {
            // Potential race condition, might throw an exception or return incorrect data
            items.removeAt(0)
        } else {
            null
        }
    }
}

fun main() {
    val buffer = SharedBuffer()
    val totalItems = 1000

    // Producer Thread
    val producer = thread {
        for (i in 1..totalItems) {
            buffer.produce(i)
            //println("produced item $i")
            // Simulate some processing delay
            Thread.sleep(1)
        }
        println("Producer finished producing $totalItems items.")
    }

    // Consumer Thread
    val consumer = thread {
        val consumedCount = AtomicInteger(0)
        while (consumedCount.get() < totalItems) {
            val item = buffer.consume()
            if (item != null) {
                println("Consumed: $item")
                consumedCount.incrementAndGet()
            } else {
                // Race condition: Consumer finds the buffer empty even though more items might be coming
                println("Buffer was empty unexpectedly!")
            }
            Thread.sleep(2)
        }
        println("Consumer finished consuming $consumedCount items.")
    }

    // Wait for both threads to finish
    producer.join()
    consumer.join()

    println("Main thread finished.")
}
