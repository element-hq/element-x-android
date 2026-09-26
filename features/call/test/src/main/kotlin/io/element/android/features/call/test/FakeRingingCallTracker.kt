/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.test

import io.element.android.features.call.api.CallData
import io.element.android.features.call.api.RingingCallTracker

class FakeRingingCallTracker : RingingCallTracker {
    val joined = mutableListOf<CallData>()
    val ended = mutableListOf<CallData>()

    override suspend fun onCallJoined(callData: CallData) {
        joined += callData
    }

    override suspend fun onCallEnded(callData: CallData) {
        ended += callData
    }
}
