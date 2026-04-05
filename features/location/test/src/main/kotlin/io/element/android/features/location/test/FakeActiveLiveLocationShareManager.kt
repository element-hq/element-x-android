/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.test

import io.element.android.features.location.api.Location
import io.element.android.features.location.api.live.ActiveLiveLocationShare
import io.element.android.features.location.api.live.ActiveLiveLocationShareManager
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

class FakeActiveLiveLocationShareManager : ActiveLiveLocationShareManager {
    private val _activeShares = MutableStateFlow<Map<RoomId, ActiveLiveLocationShare>>(emptyMap())
    override val activeShares: StateFlow<Map<RoomId, ActiveLiveLocationShare>> = _activeShares

    var startShareResult: Result<Unit> = Result.success(Unit)
    var stopShareResult: Result<Unit> = Result.success(Unit)

    val startShareCalls = mutableListOf<Triple<SessionId, RoomId, Long>>()
    val stopShareCalls = mutableListOf<Pair<SessionId, RoomId>>()
    val locationUpdateCalls = mutableListOf<Location>()

    override suspend fun startShare(roomId: RoomId, duration: Duration): Result<Unit> {
        startShareCalls += Pair(sessionId, roomId, duration.inWholeMilliseconds)
        return startShareResult
    }

    override suspend fun stopShare(roomId: RoomId): Result<Unit> {
        stopShareCalls += Pair(sessionId, roomId)
        return stopShareResult
    }

    fun givenActiveShare(share: ActiveLiveLocationShare) {
        _activeShares.value = _activeShares.value + (share.roomId to share)
    }

    fun givenNoActiveShares() {
        _activeShares.value = emptyMap()
    }
}
