/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.toolbox.test.systemclock

import io.element.android.services.toolbox.api.systemclock.SystemClock

const val A_FAKE_TIMESTAMP = 123L

class FakeSystemClock : SystemClock {
    override fun epochMillis(): Long {
        return A_FAKE_TIMESTAMP
    }
}
