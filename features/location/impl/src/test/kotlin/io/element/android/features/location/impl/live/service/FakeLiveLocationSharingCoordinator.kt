/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live.service

import io.element.android.features.location.api.Location
import io.element.android.libraries.matrix.api.core.SessionId

class FakeLiveLocationSharingCoordinator : LiveLocationSharingCoordinator {
    val registeredSessionIds = mutableListOf<SessionId>()
    val dispatchedLocations = mutableListOf<Location>()
    private val receivers = linkedMapOf<SessionId, LiveLocationReceiver>()

    override fun register(sessionId: SessionId, receiver: LiveLocationReceiver) {
        registeredSessionIds += sessionId
        receivers[sessionId] = receiver
    }

    override fun unregister(sessionId: SessionId) {
        registeredSessionIds.remove(sessionId)
        receivers.remove(sessionId)
    }

    override suspend fun dispatch(location: Location) {
        dispatchedLocations += location
        receivers.values.forEach { receiver ->
            receiver.onLocationUpdate(location)
        }
    }
}
