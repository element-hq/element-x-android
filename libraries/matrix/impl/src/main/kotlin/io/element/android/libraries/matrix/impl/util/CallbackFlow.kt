/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.util

import io.element.android.libraries.core.data.tryOrNull
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.matrix.rustcomponents.sdk.TaskHandle

internal fun <T> mxCallbackFlow(block: suspend ProducerScope<T>.() -> TaskHandle) =
    callbackFlow {
        val taskHandle: TaskHandle? = tryOrNull {
            block(this)
        }
        awaitClose {
            taskHandle?.cancelAndDestroy()
        }
    }
