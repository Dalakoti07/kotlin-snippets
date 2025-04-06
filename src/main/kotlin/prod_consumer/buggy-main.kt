package prod_consumer

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

/*
// Shared buffer without synchronization
class FixedBuffer(private val capacity: Int = 10) {
    // Create a blocking queue with fixed capacity.
    private val queue: BlockingQueue<Int> = ArrayBlockingQueue(capacity)

    // Produces an item by putting it into the queue. Blocks if full.
    fun produce(item: Int) {
        queue.put(item)  // Blocks if the queue is full.
        println("Produced: $item | Buffer size: ${queue.size}")
    }

    // Consumes an item by taking it from the queue. Blocks if empty.
    fun consume(): Int {
        val item = queue.take()  // Blocks if the queue is empty.
        println("Consumed: $item | Buffer size: ${queue.size}")
        return item
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
 */