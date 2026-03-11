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
    private val lock: (key: String, time: Duration) -> Unit = { _, _ -> },
    private val unlock: (key: String) -> Unit = { _ -> },
) : PushHandlingWakeLock {
    override fun lock(key: String, time: Duration) {
        lock.invoke(key, time)
    }

    override fun unlock(key: String) {
        unlock.invoke(key)
    }
}
