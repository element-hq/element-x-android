package io.element.android.x.architecture

import androidx.compose.runtime.MutableState

sealed interface Async<out T> {
    object Uninitialized : Async<Nothing>
    data class Loading<out T>(val prevState: T? = null) : Async<T>
    data class Failure<out T>(val error: Throwable) : Async<T>
    data class Success<out T>(val state: T) : Async<T>
}

suspend fun <T> (suspend () -> T).execute(state: MutableState<Async<T>>) {
    try {
        state.value = Async.Loading()
        state.value = Async.Success(this())
    } catch (error: Throwable) {
        state.value = Async.Failure(error)
    }
}

suspend fun <T> (suspend () -> Result<T>).executeResult(state: MutableState<Async<T>>) {
    state.value = Async.Loading()
    this().fold(
        onSuccess = {
            state.value = Async.Success(it)
        },
        onFailure = {
            state.value = Async.Failure(it)
        }
    )
}
