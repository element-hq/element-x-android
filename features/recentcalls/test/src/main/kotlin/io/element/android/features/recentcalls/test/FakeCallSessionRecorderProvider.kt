/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.test

import io.element.android.features.recentcalls.api.CallSessionRecorder
import io.element.android.features.recentcalls.api.CallSessionRecorderProvider
import io.element.android.libraries.matrix.api.core.SessionId

class FakeCallSessionRecorderProvider(
    private val recorder: CallSessionRecorder = FakeCallSessionRecorder(),
) : CallSessionRecorderProvider {
    override fun register(sessionId: SessionId, recorder: CallSessionRecorder) = Unit

    override fun unregister(sessionId: SessionId) = Unit

    override fun get(sessionId: SessionId): CallSessionRecorder? = recorder
}
