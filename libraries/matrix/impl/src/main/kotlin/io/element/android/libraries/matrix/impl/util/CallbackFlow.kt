/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.util

import io.element.android.libraries.core.data.tryOrNull
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.matrix.rustcomponents.sdk.TaskHandle
import timber.log.Timber

internal fun <T> mxCallbackFlow(block: suspend ProducerScope<T>.() -> TaskHandle) =
    callbackFlow {
        // A failed subscription leaves the flow open and silent, so at least say so.
        val taskHandle: TaskHandle? = tryOrNull(onException = { Timber.w(it, "Failed to subscribe") }) {
            block(this)
        }
        awaitClose {
            taskHandle?.cancelAndDestroy()
        }
    }
