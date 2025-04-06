package prod_consumer

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

// Shared buffer without synchronization
class FixedBuffer(private val capacity: Int = 10) {
    private val items = mutableListOf<Int>()

    // Mutex/Lock
    private val lock = ReentrantLock()

    // Condition variables
    private val notFull: Condition = lock.newCondition()
    private val notEmpty: Condition = lock.newCondition()

    fun produce(item: Int) {
        lock.withLock {
            // If the buffer is full, wait until there's space
            while (items.size == capacity) {
                println("Producer waiting... (buffer full with ${items.size} items)")
                notFull.await()
            }

            // Now we can safely add
            items.add(item)
            println("Produced: $item | Buffer size: ${items.size}")

            // Signal that there's now something to consume
            notEmpty.signal()
        }
    }

    fun consume(): Int {
        lock.withLock {
            // If the buffer is empty, wait until there's an item
            while (items.isEmpty()) {
                println("Consumer waiting... (buffer empty)")
                notEmpty.await()
            }

            // Remove item
            val removed = items.removeAt(0)
            println("Consumed: $removed | Buffer size: ${items.size}")

            // Signal producer that there's space now
            notFull.signal()

            return removed
        }
    }
}

fun main() {
    val buffer = FixedBuffer()
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
