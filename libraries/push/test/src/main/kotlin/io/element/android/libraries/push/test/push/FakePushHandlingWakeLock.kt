/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.push

import io.element.android.libraries.push.api.push.PushHandlingWakeLock

class FakePushHandlingWakeLock(
    private val lock: () -> Unit = {},
    private val unlock: () -> Unit = {},
) : PushHandlingWakeLock {
    override fun lock() {
        lock.invoke()
    }

    override suspend fun unlock() {
        unlock.invoke()
    }
}
