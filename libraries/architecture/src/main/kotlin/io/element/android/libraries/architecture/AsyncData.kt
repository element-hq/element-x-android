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
sealed interface AsyncData<out T> {
    /**
     * Represents a failed operation.
     *
     * @param T the type of data returned by the operation.
     * @property error the error that caused the operation to fail.
     * @property prevData the data returned by a previous successful run of the operation if any.
     */
    data class Failure<out T>(
        val error: Throwable,
        val prevData: T? = null,
    ) : AsyncData<T>

    /**
     * Represents an operation that is currently ongoing.
     *
     * @param T the type of data returned by the operation.
     * @property prevData the data returned by a previous successful run of the operation if any.
     */
    data class Loading<out T>(
        val prevData: T? = null,
    ) : AsyncData<T>

    /**
     * Represents a successful operation.
     *
     * @param T the type of data returned by the operation.
     * @property data the data returned by the operation.
     */
    data class Success<out T>(
        val data: T,
    ) : AsyncData<T>

    /**
     * Represents an uninitialized operation (i.e. yet to be run).
     */
    data object Uninitialized : AsyncData<Nothing>

    /**
     * Returns the data returned by the operation, or null otherwise.
     *
     * Please note this method may return stale data if the operation is not [Success].
     */
    fun dataOrNull(): T? = when (this) {
        is Failure -> prevData
        is Loading -> prevData
        is Success -> data
        Uninitialized -> null
    }

    /**
     * Returns the error that caused the operation to fail, or null otherwise.
     */
    fun errorOrNull(): Throwable? = when (this) {
        is Failure -> error
        else -> null
    }

    fun isFailure(): Boolean = this is Failure<T>

    fun isLoading(): Boolean = this is Loading<T>

    fun isSuccess(): Boolean = this is Success<T>

    fun isUninitialized(): Boolean = this == Uninitialized

    fun isReady() = isSuccess() || isFailure()
}

suspend inline fun <T> MutableState<AsyncData<T>>.runCatchingUpdatingState(
    errorTransform: (Throwable) -> Throwable = { it },
    block: () -> T,
): Result<T> = runUpdatingState(
    state = this,
    errorTransform = errorTransform,
    resultBlock = {
        runCatching {
            block()
        }
    },
)

suspend inline fun <T> (suspend () -> T).runCatchingUpdatingState(
    state: MutableState<AsyncData<T>>,
    errorTransform: (Throwable) -> Throwable = { it },
): Result<T> = runUpdatingState(
    state = state,
    errorTransform = errorTransform,
    resultBlock = {
        runCatching {
            this()
        }
    },
)

suspend inline fun <T> MutableState<AsyncData<T>>.runUpdatingState(
    errorTransform: (Throwable) -> Throwable = { it },
    resultBlock: () -> Result<T>,
): Result<T> = runUpdatingState(
    state = this,
    errorTransform = errorTransform,
    resultBlock = resultBlock,
)

/**
 * Calls the specified [Result]-returning function [resultBlock]
 * encapsulating its progress and return value into an [AsyncData] while
 * posting its updates to the MutableState [state].
 *
 * @param T the type of data returned by the operation.
 * @param state the [MutableState] to post updates to.
 * @param errorTransform a function to transform the error before posting it.
 * @param resultBlock a suspending function that returns a [Result].
 * @return the [Result] returned by [resultBlock].
 */
@OptIn(ExperimentalContracts::class)
@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
suspend inline fun <T> runUpdatingState(
    state: MutableState<AsyncData<T>>,
    errorTransform: (Throwable) -> Throwable = { it },
    resultBlock: suspend () -> Result<T>,
): Result<T> {
    contract {
        callsInPlace(resultBlock, InvocationKind.EXACTLY_ONCE)
    }
    val prevData = state.value.dataOrNull()
    state.value = AsyncData.Loading(prevData = prevData)
    return resultBlock().fold(
        onSuccess = {
            state.value = AsyncData.Success(it)
            Result.success(it)
        },
        onFailure = {
            val error = errorTransform(it)
            state.value = AsyncData.Failure(
                error = error,
                prevData = prevData,
            )
            Result.failure(error)
        }
    )
}
