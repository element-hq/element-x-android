/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.core.extensions

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
    return mapCatching(transform).fold(
        onSuccess = { it },
        onFailure = { Result.failure(it) }
    )
}

inline fun <T> Result<T>.finally(block: (exception: Throwable?) -> Unit): Result<T> {
    onSuccess { block(null) }
    onFailure(block)
    return this
}
