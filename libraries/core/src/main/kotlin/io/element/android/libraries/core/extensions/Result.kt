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
