/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.test

import io.element.android.features.ptt.api.PttSessionManager
import io.element.android.features.ptt.api.PttSessionState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePttSessionManager(
    private val sessionStateFlow: MutableStateFlow<PttSessionState?> = MutableStateFlow(null),
) : PttSessionManager {
    override val sessionState: StateFlow<PttSessionState?> = sessionStateFlow

    override fun start(sessionId: SessionId, roomId: RoomId) = Unit

    override fun stop() = Unit

    override fun pressToTalk() = Unit

    override fun releaseToTalk() = Unit
}
