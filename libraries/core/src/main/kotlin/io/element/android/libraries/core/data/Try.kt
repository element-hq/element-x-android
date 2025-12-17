/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.data

import kotlin.coroutines.cancellation.CancellationException

/**
 * Can be used to catch [Exception]s in a block of code, returning `null` if an exception occurs.
 *
 * If the block throws a [CancellationException], it will be rethrown.
 */
inline fun <A> tryOrNull(onException: ((Exception) -> Unit) = { }, operation: () -> A): A? {
    return try {
        operation()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        onException.invoke(e)
        null
    }
}
