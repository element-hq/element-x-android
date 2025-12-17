/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.coroutine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.runningFold

/**
 * Returns the first element of the flow that is an instance of [T], waiting for it if necessary.
 */
suspend inline fun <reified T> Flow<*>.firstInstanceOf(): T {
    return first { it is T } as T
}

/**
 * Returns a flow that emits pairs of the previous and current values.
 * The first emission will be a pair of `null` and the first value emitted by the source flow.
 */
fun <T> Flow<T>.withPreviousValue(): Flow<Pair<T?, T>> {
    return runningFold(null) { prev: Pair<T?, T>?, current ->
        prev?.second to current
    }
        .filterNotNull()
}
