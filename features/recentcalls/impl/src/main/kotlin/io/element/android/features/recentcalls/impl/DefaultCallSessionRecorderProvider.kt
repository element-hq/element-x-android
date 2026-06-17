/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.recentcalls.api.CallSessionRecorder
import io.element.android.features.recentcalls.api.CallSessionRecorderProvider
import io.element.android.libraries.matrix.api.core.SessionId
import java.util.concurrent.ConcurrentHashMap

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DefaultCallSessionRecorderProvider : CallSessionRecorderProvider {
    private val recorders = ConcurrentHashMap<SessionId, CallSessionRecorder>()

    override fun register(sessionId: SessionId, recorder: CallSessionRecorder) {
        recorders[sessionId] = recorder
    }

    override fun unregister(sessionId: SessionId) {
        recorders.remove(sessionId)
    }

    override fun get(sessionId: SessionId): CallSessionRecorder? = recorders[sessionId]
}
