/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.test

import io.element.android.features.privatepush.api.PrivatePushService
import io.element.android.features.privatepush.api.PrivatePushStatus
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePrivatePushService(
    var statusResult: PrivatePushStatus = PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NtfyNotInstalled),
) : PrivatePushService {
    val dismissed = MutableStateFlow(false)
    val requests = MutableStateFlow<Set<SessionId>>(emptySet())
    val statusCalls = mutableListOf<SessionId>()
    var fallBackToBuiltInResult: Boolean = false
    val fallBackToBuiltInCalls = mutableListOf<SessionId>()

    override suspend fun status(sessionId: SessionId): PrivatePushStatus {
        statusCalls += sessionId
        return statusResult
    }

    override suspend fun fallBackToBuiltIn(matrixClient: MatrixClient): Boolean {
        fallBackToBuiltInCalls += matrixClient.sessionId
        return fallBackToBuiltInResult
    }

    override fun isDismissed(sessionId: SessionId): Flow<Boolean> = dismissed

    override suspend fun setDismissed(sessionId: SessionId, dismissed: Boolean) {
        this.dismissed.value = dismissed
    }

    override fun setupRequested(sessionId: SessionId): Flow<Boolean> = requests.map { sessionId in it }

    override fun requestSetup(sessionId: SessionId) {
        requests.update { it + sessionId }
    }

    override fun clearSetupRequest(sessionId: SessionId) {
        requests.update { it - sessionId }
    }
}
