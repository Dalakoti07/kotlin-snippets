package flow_exp

import com.hoc081098.flowext.flatMapFirst
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

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
}*/

// above snippet is not working,
// if flatMapConcat is replaced by flatMapLatest then it works

suspend fun main() {
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

}
