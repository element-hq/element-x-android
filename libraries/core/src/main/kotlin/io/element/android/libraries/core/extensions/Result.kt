/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.extensions

import kotlin.coroutines.cancellation.CancellationException

/**
 * Can be used to catch exceptions in a block of code and return a [Result].
 * If the block throws a [CancellationException], it will be rethrown.
 * If it throws any other exception, it will be wrapped in a [Result.failure].
 *
 * [Error]s are not caught by this function, as they are not meant to be caught in normal application flow.
 */
inline fun <T> runCatchingExceptions(
    block: () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Can be used to catch exceptions in a block of code and return a [Result].
 * If the block throws a [CancellationException], it will be rethrown.
 * If it throws any other exception, it will be wrapped in a [Result.failure].
 *
 * [Error]s are not caught by this function, as they are not meant to be caught in normal application flow.
 */
inline fun <T, R> T.runCatchingExceptions(
    block: T.() -> R
): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Can be used to transform a [Result] into another [Result] by applying a [block] to the value if it is successful.
 * If the original [Result] is a failure, the exception will be wrapped in a new [Result.failure].
 *
 * This is a safer version of [Result.mapCatching].
 */
inline fun <R, T> Result<T>.mapCatchingExceptions(
    block: (T) -> R,
): Result<R> {
    return fold(
        onSuccess = { value -> runCatchingExceptions { block(value) } },
        onFailure = { exception -> Result.failure(exception) }
    )
}

/**
 * Can be used to transform some Throwable into some other.
 */
inline fun <R, T : R> Result<T>.mapFailure(transform: (exception: Throwable) -> Throwable): Result<R> {
    return when (val exception = exceptionOrNull()) {
        null -> this
        else -> Result.failure(transform(exception))
    }
}

/**
 * Can be used to apply a [transform] that returns a [Result] to a base [Result] and get another [Result].
 * @return The result of the transform as a [Result].
 */
inline fun <R, T> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    return map(transform).fold(
        onSuccess = { it },
        onFailure = { Result.failure(it) }
    )
}

/**
 * Can be used to apply a [transform] that returns a [Result] to a base [Result] and get another [Result], catching any exception.
 * @return The result of the transform or a caught exception wrapped in a [Result].
 */
inline fun <R, T> Result<T>.flatMapCatching(transform: (T) -> Result<R>): Result<R> {
    return mapCatchingExceptions(transform).fold(
        onSuccess = { it },
        onFailure = { Result.failure(it) }
    )
}

/**
 * Can be used to execute a block of code after the [Result] has been processed, regardless of whether it was successful or not.
 * The block receives the exception if there was one, or `null` if the result was successful.
 */
inline fun <T> Result<T>.finally(block: (exception: Throwable?) -> Unit): Result<T> {
    onSuccess { block(null) }
    onFailure(block)
    return this
}
