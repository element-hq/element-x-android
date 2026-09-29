/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import io.element.android.call.api.ElementCallController
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * @param controller what [getOrBuild] answers with, or null to stand for a session that cannot be
 * restored - logged out, or an id from a stale notification.
 */
class FakeElementCallControllers(
    private val controller: ElementCallController? = null,
) : ElementCallControllers {
    val requestedSessions = mutableListOf<SessionId>()

    override suspend fun getOrBuild(sessionId: SessionId): ElementCallController? {
        requestedSessions += sessionId
        return controller
    }

    override fun controller(sessionId: SessionId): Flow<ElementCallController?> = flowOf(controller)

    override fun withRunningCall(): ElementCallController? =
        controller?.takeIf { it.state.value != null }
}
