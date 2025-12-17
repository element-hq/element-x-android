/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.test.systemclock

import io.element.android.services.toolbox.api.systemclock.SystemClock

const val A_FAKE_TIMESTAMP = 123L

class FakeSystemClock(
    var epochMillisResult: Long = A_FAKE_TIMESTAMP
) : SystemClock {
    override fun epochMillis() = epochMillisResult
}
