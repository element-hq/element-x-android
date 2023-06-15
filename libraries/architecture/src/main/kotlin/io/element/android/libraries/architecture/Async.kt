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
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Sealed type that allows to model an asynchronous operation.
 */
@Stable
sealed interface Async<out T> {
    data class Failure<out T>(
        val exception: Throwable,
        val prevData: T? = null,
    ) : Async<T>

    data class Loading<out T>(
        val prevData: T? = null,
    ) : Async<T>

    data class Success<out T>(
        val data: T,
    ) : Async<T>

    object Uninitialized : Async<Nothing>

    fun dataOrNull(): T? = when (this) {
        is Failure -> prevData
        is Loading -> prevData
        is Success -> data
        Uninitialized -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Failure -> exception
        else -> null
    }

    fun isFailure(): Boolean = this is Failure<T>

    fun isLoading(): Boolean = this is Loading<T>

    fun isSuccess(): Boolean = this is Success<T>

    fun isUninitialized(): Boolean = this == Uninitialized
}

suspend inline fun <T> MutableState<Async<T>>.runCatchingUpdatingState(
    exceptionTransform: (Throwable) -> Throwable = { it },
    block: () -> T,
): Result<T> = runUpdatingState(
    state = this,
    exceptionTransform = exceptionTransform,
    resultBlock = {
        runCatching {
            block()
        }
    },
)

suspend inline fun <T> (suspend () -> T).runCatchingUpdatingState(
    state: MutableState<Async<T>>,
    exceptionTransform: (Throwable) -> Throwable = { it },
): Result<T> = runUpdatingState(
    state = state,
    exceptionTransform = exceptionTransform,
    resultBlock = {
        runCatching {
            this()
        }
    },
)

suspend inline fun <T> MutableState<Async<T>>.runUpdatingState(
    exceptionTransform: (Throwable) -> Throwable = { it },
    resultBlock: () -> Result<T>,
): Result<T> = runUpdatingState(
    state = this,
    exceptionTransform = exceptionTransform,
    resultBlock = resultBlock,
)

/**
 * Calls the specified [Result]-returning function [resultBlock]
 * encapsulating its progress and return value into an [Async] while
 * posting its updates to the MutableState [state].
 *
 * @state the [MutableState] to post updates to.
 * @exceptionTransform a function to transform the exception before posting it.
 * @resultBlock a suspending function that returns a [Result].
 * @return the [Result] returned by [resultBlock].
 */
@OptIn(ExperimentalContracts::class)
@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
suspend inline fun <T> runUpdatingState(
    state: MutableState<Async<T>>,
    exceptionTransform: (Throwable) -> Throwable = { it },
    resultBlock: suspend () -> Result<T>,
): Result<T> {
    contract {
        callsInPlace(resultBlock, InvocationKind.EXACTLY_ONCE)
    }
    val prevData = state.value.dataOrNull()
    state.value = Async.Loading(prevData = prevData)
    val result = resultBlock()
    return if (result.isSuccess) {
        val data = result.getOrNull()!!
        state.value = Async.Success(data)
        Result.success(data)
    } else {
        val exception = exceptionTransform(result.exceptionOrNull()!!)
        state.value = Async.Failure(
            exception = exception,
            prevData = prevData,
        )
        Result.failure(exception)
    }
}
