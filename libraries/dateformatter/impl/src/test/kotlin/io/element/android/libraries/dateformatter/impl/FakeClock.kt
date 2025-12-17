/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import kotlin.time.Clock
import kotlin.time.Instant

class FakeClock : Clock {
    private var instant: Instant = Instant.fromEpochMilliseconds(0)

    fun givenInstant(instant: Instant) {
        this.instant = instant
    }

    override fun now(): Instant = instant
}
