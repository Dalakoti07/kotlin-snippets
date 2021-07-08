package coroutines.suspendFunctions

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun main() {

  getUserFromNetworkCallback("101") { user, error ->
    user?.run(::println)
    error?.printStackTrace()
  }
  println("main end")

  GlobalScope.launch {
    val user = getUserSuspend("101")
    println(user)
  }

}

fun getUserSuspend(userId:String):User{
  Thread.sleep(1000);
  return User(userId,"Filip")
}

/*suspend fun getUserSuspend(userId: String): User {
  delay(1000)

  return User(userId, "Filip")
}*/

fun getUserFromNetworkCallback(
  userId: String,
  onUserResponse: (User?, Throwable?) -> Unit) {
  thread {

    try {
      Thread.sleep(1000)
      val user = User(userId, "Filip")

      onUserResponse(user, null)
    } catch (error: Throwable) {
      onUserResponse(null, error)
    }
  }
}

suspend fun readFileSuspend(path: String): File =
    suspendCoroutine {
      readFile(path) { file ->
        it.resume(file)
      }
    }

fun readFile(path: String, onReady: (File) -> Unit) {
  Thread.sleep(1000)
  // some heavy operation

  onReady(File(path))
}