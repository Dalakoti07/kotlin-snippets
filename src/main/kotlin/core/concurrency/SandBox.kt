package core.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.random.Random

class PlayGround {

    private val items = LinkedList<Int>()
    private val mutex = Mutex()

    fun run(){
        val coroutinesScope1 = CoroutineScope(Dispatchers.Default)
        val coroutinesScope2 = CoroutineScope(Dispatchers.Default)
        val coroutinesScope3 = CoroutineScope(Dispatchers.Default)
        items.addAll(listOf(1,2,3,5,6,7,8,9))

        coroutinesScope3.launch {
            while (true){
                delay(500)
                println("added 4 elems")
                mutex.withLock {
                    items.addAll(listOf(
                        Random.nextInt(),
                        Random.nextInt(),
                        Random.nextInt(),
                        Random.nextInt(),
                    ))
                }
            }
        }

        coroutinesScope2.launch {
            while (true){
                delay(1000)
                removeItem()
            }
        }
        coroutinesScope1.launch {
            while (true){
                delay(500)
                removeAll()
            }
        }
    }

    private suspend fun removeItem() {
        mutex.withLock {
            if(items.isEmpty()) {
                println("is empty")
                return
            }
            delay(500)
            val last = items.last
            println("$last removed ....")
        }
    }

    private suspend fun removeAll() {
        mutex.withLock {
            items.clear()
            println("removeAll ....")
        }
    }


}