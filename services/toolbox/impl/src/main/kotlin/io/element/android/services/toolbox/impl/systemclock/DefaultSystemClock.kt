/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.impl.systemclock

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.services.toolbox.api.systemclock.SystemClock

@ContributesBinding(AppScope::class)
class DefaultSystemClock : SystemClock {
    /**
     * Provides a UTC epoch in milliseconds
     *
     * This value is not guaranteed to be correct with reality
     * as a User can override the system time and date to any values.
     */
    override fun epochMillis(): Long {
        return System.currentTimeMillis()
    }
}
