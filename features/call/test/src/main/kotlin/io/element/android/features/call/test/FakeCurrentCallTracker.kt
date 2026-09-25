/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.test

import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.api.CurrentCallTracker

class FakeCurrentCallTracker : CurrentCallTracker {
    /** Every transition in order, so a test can assert the call was reported before it was cleared. */
    val calls = mutableListOf<CurrentCall>()

    override fun onCallStarted(call: CurrentCall) {
        calls += call
    }

    override fun onCallEnded() {
        calls += CurrentCall.None
    }
}
