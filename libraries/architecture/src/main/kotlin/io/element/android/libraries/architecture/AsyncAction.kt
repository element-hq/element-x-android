/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.architecture

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Sealed type that allows to model an asynchronous operation triggered by the user.
 */
@Stable
sealed interface AsyncAction<out T> {
    /**
     * Represents an uninitialized operation (i.e. yet to be run by the user).
     */
    data object Uninitialized : AsyncAction<Nothing>

    /**
     * Represents an operation that is currently waiting for user confirmation.
     */
    interface Confirming : AsyncAction<Nothing>

    data object ConfirmingNoParams : Confirming

    /**
     * Represents an operation that is currently ongoing.
     */
    data object Loading : AsyncAction<Nothing>

    /**
     * Represents a failed operation.
     *
     * @property error the error that caused the operation to fail.
     */
    data class Failure(
        val error: Throwable,
    ) : AsyncAction<Nothing>

    /**
     * Represents a successful operation.
     *
     * @param T the type of data returned by the operation.
     * @property data the data returned by the operation.
     */
    data class Success<out T>(
        val data: T,
    ) : AsyncAction<T>

    /**
     * Returns the data returned by the operation, or null otherwise.
     */
    fun dataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Returns the error that caused the operation to fail, or null otherwise.
     */
    fun errorOrNull(): Throwable? = when (this) {
        is Failure -> error
        else -> null
    }

    fun isUninitialized(): Boolean = this == Uninitialized

    fun isConfirming(): Boolean = this is Confirming

    fun isLoading(): Boolean = this == Loading

    fun isFailure(): Boolean = this is Failure

    fun isSuccess(): Boolean = this is Success

    fun isReady() = isSuccess() || isFailure()
}

suspend inline fun <T> MutableState<AsyncAction<T>>.runCatchingUpdatingState(
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
    state: MutableState<AsyncAction<T>>,
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

suspend inline fun <T> MutableState<AsyncAction<T>>.runUpdatingState(
    errorTransform: (Throwable) -> Throwable = { it },
    resultBlock: () -> Result<T>,
): Result<T> = runUpdatingState(
    state = this,
    errorTransform = errorTransform,
    resultBlock = resultBlock,
)

/**
 * Run the given block and update the state accordingly, using only Loading and Failure states.
 * It's up to the caller to manage the Success state.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> MutableState<AsyncAction<T>>.runUpdatingStateNoSuccess(
    resultBlock: () -> Result<Unit>,
): Result<Unit> {
    contract {
        callsInPlace(resultBlock, InvocationKind.EXACTLY_ONCE)
    }
    value = AsyncAction.Loading
    return resultBlock()
        .onFailure { failure ->
            value = AsyncAction.Failure(failure)
        }
}

/**
 * Calls the specified [Result]-returning function [resultBlock]
 * encapsulating its progress and return value into an [AsyncAction] while
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
    state: MutableState<AsyncAction<T>>,
    errorTransform: (Throwable) -> Throwable = { it },
    resultBlock: suspend () -> Result<T>,
): Result<T> {
    contract {
        callsInPlace(resultBlock, InvocationKind.EXACTLY_ONCE)
    }
    state.value = AsyncAction.Loading
    return resultBlock().fold(
        onSuccess = {
            state.value = AsyncAction.Success(it)
            Result.success(it)
        },
        onFailure = {
            val error = errorTransform(it)
            state.value = AsyncAction.Failure(
                error = error,
            )
            Result.failure(error)
        }
    )
}
