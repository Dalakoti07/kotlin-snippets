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
    val buffer = FixedBuffer(10)

    // Settings: 3 producers and 3 consumers.
    val totalProducers = 3
    val totalConsumers = 3
    // Each producer produces 20 items (total 60 items).
    val itemsPerProducer = 20
    // Each consumer consumes 20 items (total 60 items).
    val itemsPerConsumer = 20

    // List to keep track of producer and consumer threads.
    val producers = mutableListOf<Thread>()
    val consumers = mutableListOf<Thread>()

    // Launch multiple producers.
    for (p in 1..totalProducers) {
        val producerThread = thread(start = true) {
            for (i in 1..itemsPerProducer) {
                // Unique item number: calculates an overall sequence.
                val item = (p - 1) * itemsPerProducer + i
                buffer.produce(item)
                Thread.sleep(50) // Fast production pace.
            }
            println("Producer $p finished producing $itemsPerProducer items.")
        }
        producers.add(producerThread)
    }

    // Launch multiple consumers.
    for (c in 1..totalConsumers) {
        val consumerThread = thread(start = true) {
            for (i in 1..itemsPerConsumer) {
                buffer.consume()
                Thread.sleep(100) // Slower consumption pace.
            }
            println("Consumer $c finished consuming $itemsPerConsumer items.")
        }
        consumers.add(consumerThread)
    }

    // Wait for all threads to complete.
    producers.forEach { it.join() }
    consumers.forEach { it.join() }

    println("Main thread finished.")
}
