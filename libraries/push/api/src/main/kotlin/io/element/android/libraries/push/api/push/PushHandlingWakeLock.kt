/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.push

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Abstraction over wakelocks used for push handling to ensure the device stays awake while we handle the push and schedule and run the work.
 */
interface PushHandlingWakeLock {
    /**
     * Acquire a wakelock for the given [key]. The wakelock will be held for the given [time] or until [unlock] is called, whichever happens first.
     */
    fun lock(key: String, time: Duration = 1.minutes)

    /**
     * Release the wakelock associated with the given [key]. If no wakelock is associated with the key, this method does nothing.
     */
    fun unlock(key: String)
}
