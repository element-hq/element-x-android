/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.push

import io.element.android.libraries.push.api.push.PushHandlingWakeLock
import kotlin.time.Duration

class FakePushHandlingWakeLock(
    private val lock: (time: Duration) -> Unit = {},
    private val unlock: () -> Unit = {},
) : PushHandlingWakeLock {
    override fun lock(time: Duration) {
        lock.invoke(time)
    }

    override suspend fun unlock() {
        unlock.invoke()
    }
}
