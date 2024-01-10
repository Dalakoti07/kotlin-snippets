package core.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class PlayGround {

    private val items = LinkedList<Int>()

    fun run(){
        val coroutinesScope1 = CoroutineScope(Dispatchers.Default)
        val coroutinesScope2 = CoroutineScope(Dispatchers.Default)
        items.addAll(listOf(1,2,3,5,6))

        coroutinesScope1.launch {
            removeAll()
        }
        coroutinesScope2.launch {
            removeItem()
        }
    }

    private suspend fun removeItem() {
        if(items.isEmpty()) {
            return
        }
        delay(1000)
        val last = items.last()
        items.remove()
        println("removeItem ....")
        delay(1000)
        removeItem()
    }

    private suspend fun removeAll() {
        delay(500)
        items.clear()
        println("removeAll ....")
        removeAll()
    }


}