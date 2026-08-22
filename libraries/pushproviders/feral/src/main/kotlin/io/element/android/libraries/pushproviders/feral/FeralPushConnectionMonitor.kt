/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Live state of the connection service, for the troubleshoot screen. */
data class FeralPushServiceStatus(
    val serviceRunning: Boolean = false,
    val connectedSessions: Set<SessionId> = emptySet(),
) {
    fun isConnected(sessionId: SessionId): Boolean = sessionId in connectedSessions
}

/** In-memory, app-wide mirror of what FeralPushConnectionService is doing. */
@SingleIn(AppScope::class)
@Inject
class FeralPushConnectionMonitor {
    private val _status = MutableStateFlow(FeralPushServiceStatus())
    val status: StateFlow<FeralPushServiceStatus> = _status.asStateFlow()

    fun setServiceRunning(running: Boolean) {
        _status.update { if (running) it.copy(serviceRunning = true) else FeralPushServiceStatus() }
    }

    fun setConnected(sessionId: SessionId, connected: Boolean) {
        _status.update {
            it.copy(connectedSessions = if (connected) it.connectedSessions + sessionId else it.connectedSessions - sessionId)
        }
    }
}
