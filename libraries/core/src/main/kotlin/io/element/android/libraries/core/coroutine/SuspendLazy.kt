/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SuspendLazy<T>(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val block: suspend CoroutineScope.() -> T,
) {
    private val coroutineScope = CoroutineScope(coroutineContext)

    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): Deferred<T> {
        return coroutineScope.async(start = CoroutineStart.LAZY, block = block)
    }
}

fun <T> suspendLazy(coroutineContext: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): SuspendLazy<T> {
    return SuspendLazy(coroutineContext, block = block)
}
