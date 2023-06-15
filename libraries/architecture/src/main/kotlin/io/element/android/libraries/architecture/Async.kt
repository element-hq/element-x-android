/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.architecture

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable

@Stable
sealed interface Async<out T> {
    object Uninitialized : Async<Nothing>
    data class Loading<out T>(val prevState: T? = null) : Async<T>
    data class Failure<out T>(val error: Throwable, val prevState: T? = null) : Async<T>
    data class Success<out T>(val state: T) : Async<T>

    fun dataOrNull(): T? {
        return when (this) {
            is Failure -> prevState
            is Loading -> prevState
            is Success -> state
            Uninitialized -> null
        }
    }
}

suspend inline fun <T> (suspend () -> T).execute(
    state: MutableState<Async<T>>,
    errorMapping: ((Throwable) -> Throwable) = { it },
) {
    try {
        state.value = Async.Loading()
        val result = this()
        state.value = Async.Success(result)
    } catch (error: Throwable) {
        state.value = Async.Failure(errorMapping.invoke(error))
    }
}

suspend inline fun <T> (suspend () -> Result<T>).executeResult(state: MutableState<Async<T>>) {
    if (state.value !is Async.Success) {
        state.value = Async.Loading()
    }
    this().fold(
        onSuccess = {
            state.value = Async.Success(it)
        },
        onFailure = {
            state.value = Async.Failure(it)
        }
    )
}

fun <T> Async<T>.isLoading(): Boolean {
    return this is Async.Loading<T>
}
