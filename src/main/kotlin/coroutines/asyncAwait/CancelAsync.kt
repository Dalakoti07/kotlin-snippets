package coroutines.asyncAwait

import kotlinx.coroutines.*


fun main() {
    GlobalScope.launch(context = Dispatchers.Main) {
        val data = produceValue<Int>(this)

        data.await()
    }
}

fun <T> produceValue(scope: CoroutineScope): Deferred<T> =
    scope.async {
        var data: T? = null

        requestData<T> { value ->
            data = value
        }

        while (data == null && scope.isActive) {
            // loop for data, while the scope is active
        }

        data!!
    }

fun <T> requestData(callback : (T)->Unit){
    // make api call
}