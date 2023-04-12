package flow_exp

import kotlinx.coroutines.flow.*

/*
SideEffect<VerifyOtpState, VerifyOtpAction> { actionFlow, stateFlow, _ ->
    actionFlow.filterIsInstance<VerifyOtpAction.VerifyOtpButtonPressed>()
        .filter {
            stateFlow.value.enteredOtp.isNotEmpty()
        }
        .flatMapConcat {
            executeLoginUseCase(
                number = stateFlow.value.phoneNumber,
                enteredOtp = stateFlow.value.enteredOtp,
            )
        }
}
*/

// above snippet is not working,
// if flatMapConcat is replaced by flatMapLatest then it works

suspend fun main() {
    println("........... when using concat ...........")
    flow {
        emit("saurabh")
        for (i in 1..10) {
            emit(i)
        }
        emit("dalakoti")
    }
        .filterIsInstance<Int>()
        .flatMapConcat {
            flowFrom("upstream:$it")
        }
        .collect {
            println(it)
        }

    println("........... when using latest ...........")

    flow {
        emit("saurabh")
        for (i in 1..10) {
            emit(i)
        }
        emit("dalakoti")
    }
        .filterIsInstance<Int>()
        .flatMapLatest {
            flowFrom("upstream:$it")
        }
        .collect {
            println(it)
        }
}
