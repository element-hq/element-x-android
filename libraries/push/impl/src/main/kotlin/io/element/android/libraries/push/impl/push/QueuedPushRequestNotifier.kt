/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Tells the background work that a push has been queued for a session, so it doesn't keep the work fetching it waiting.
 */
@SingleIn(AppScope::class)
@Inject
class QueuedPushRequestNotifier {
    private val _queued = MutableSharedFlow<SessionId>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val queued: SharedFlow<SessionId> = _queued

    fun onPushRequestQueued(sessionId: SessionId) {
        _queued.tryEmit(sessionId)
    }
}
