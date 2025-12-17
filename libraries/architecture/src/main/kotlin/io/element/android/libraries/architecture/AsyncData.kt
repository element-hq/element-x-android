/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import io.element.android.libraries.core.extensions.runCatchingExceptions

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
        runCatchingExceptions {
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
        runCatchingExceptions {
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
@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
suspend inline fun <T> runUpdatingState(
    state: MutableState<AsyncData<T>>,
    errorTransform: (Throwable) -> Throwable = { it },
    resultBlock: suspend () -> Result<T>,
): Result<T> {
    // Restore when the issue with contracts and AGP 8.13.x is fixed
//    contract {
//        callsInPlace(resultBlock, InvocationKind.EXACTLY_ONCE)
//    }
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

inline fun <T, R> AsyncData<T>.map(
    transform: (T) -> R,
): AsyncData<R> {
    return when (this) {
        is AsyncData.Failure -> AsyncData.Failure(
            error = error,
            prevData = prevData?.let { transform(prevData) }
        )
        is AsyncData.Loading -> AsyncData.Loading(prevData?.let { transform(prevData) })
        is AsyncData.Success -> AsyncData.Success(transform(data))
        AsyncData.Uninitialized -> AsyncData.Uninitialized
    }
}
