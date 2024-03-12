package core.concurrency

import kotlinx.coroutines.*
import java.util.*


fun main() = runBlocking{
    val ground = PlayGround()
    ground.run()
    delay(60_000)
}
