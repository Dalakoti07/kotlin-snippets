package prod_consumer

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

// Shared buffer without synchronization
class SharedBuffer {
    private val items = mutableListOf<Int>()
    private val lock = ReentrantLock()

    // No synchronization around items.add(...)
    fun produce(item: Int) {
        lock.withLock {
            items.add(item)
        }
    }

    // No synchronization around items.removeAt(...)
    fun consume(): Int? {
        lock.withLock {
            return if (items.isNotEmpty()) {
                // Potential race condition, might throw an exception or return incorrect data
                items.removeAt(0)
            } else {
                null
            }
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
        var consumedCount = 0
        while (consumedCount < totalItems) {
            val item = buffer.consume()
            if (item != null) {
                println("Consumed: $item")
                consumedCount++
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
