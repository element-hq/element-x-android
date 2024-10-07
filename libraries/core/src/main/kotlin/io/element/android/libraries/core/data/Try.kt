/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.core.data

inline fun <A> tryOrNull(onError: ((Throwable) -> Unit) = { }, operation: () -> A): A? {
    return try {
        operation()
    } catch (any: Throwable) {
        onError.invoke(any)
        null
    }
}
