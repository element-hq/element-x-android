/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.util

import org.matrix.rustcomponents.sdk.TaskHandle
import java.util.concurrent.CopyOnWriteArraySet

fun TaskHandle.cancelAndDestroy() {
    cancel()
    destroy()
}

class TaskHandleBag(private val taskHandles: MutableSet<TaskHandle> = CopyOnWriteArraySet()) : Set<TaskHandle> by taskHandles {
    operator fun plusAssign(taskHandle: TaskHandle?) {
        if (taskHandle == null) return
        taskHandles += taskHandle
    }

    fun dispose() {
        taskHandles.forEach {
            it.cancelAndDestroy()
        }
        taskHandles.clear()
    }
}
