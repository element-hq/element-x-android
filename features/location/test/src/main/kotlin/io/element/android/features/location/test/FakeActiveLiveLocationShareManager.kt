/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.test

import io.element.android.features.location.api.live.ActiveLiveLocationShareManager
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration

class FakeActiveLiveLocationShareManager(
    private val sessionId: SessionId,
    val setupLambda: () -> Unit = { lambdaError() },
    val startShareLambda: (roomId: RoomId, duration: Duration) -> Result<Unit> = { _, _ -> lambdaError() },
    val stopShareLambda: (roomId: RoomId) -> Result<Unit> = { _ -> lambdaError() },
) : ActiveLiveLocationShareManager {
    val startShareCalls = mutableListOf<Triple<SessionId, RoomId, Long>>()

    private val _activeShares = MutableStateFlow(emptySet<RoomId>())
    override val activeShares: StateFlow<Set<RoomId>> = _activeShares

    override fun setup() {
        setupLambda()
    }

    override suspend fun startShare(roomId: RoomId, duration: Duration): Result<Unit> = simulateLongTask {
        startShareCalls += Triple(sessionId, roomId, duration.inWholeMilliseconds)
        startShareLambda(roomId, duration).onSuccess {
            _activeShares.update {
                it + roomId
            }
        }
    }

    override suspend fun stopShare(roomId: RoomId): Result<Unit> = simulateLongTask {
        stopShareLambda(roomId).onSuccess {
            _activeShares.update {
                it - roomId
            }
        }
    }
}
