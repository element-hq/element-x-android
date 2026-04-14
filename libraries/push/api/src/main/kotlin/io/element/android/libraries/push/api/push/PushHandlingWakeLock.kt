/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.push

/**
 * Abstraction over wakelocks used for push handling to ensure the device stays awake while we handle the push and schedule and run the work.
 */
interface PushHandlingWakeLock {
    /**
     * Acquire a wakelock. The wakelock will be held until [unlock] is called.
     */
    fun lock()

    /**
     * Release the wakelock. If no wakelock is associated with the key, this method does nothing.
     */
    suspend fun unlock()
}
