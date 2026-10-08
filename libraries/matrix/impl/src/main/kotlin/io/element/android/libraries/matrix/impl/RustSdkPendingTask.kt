/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import io.element.android.libraries.matrix.api.SdkPendingTask
import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import org.matrix.rustcomponents.sdk.TaskHandle
import java.util.concurrent.atomic.AtomicBoolean

class RustSdkPendingTask(
    private val taskHandle: TaskHandle,
) : SdkPendingTask {
    private val closed = AtomicBoolean(false)

    override fun isRunning(): Boolean = !closed.get() && !taskHandle.isFinished()

    override fun close() {
        if (!closed.getAndSet(true)) {
            taskHandle.cancelAndDestroy()
        }
    }
}
