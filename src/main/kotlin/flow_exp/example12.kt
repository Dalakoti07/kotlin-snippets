package flow_exp

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


