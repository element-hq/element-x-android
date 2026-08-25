/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.api.systemclock

/**
 * Reads the current time, injected rather than called statically so that tests can control it.
 */
fun interface SystemClock {
    /** The current wall-clock time in milliseconds since the epoch. */
    fun epochMillis(): Long
}
