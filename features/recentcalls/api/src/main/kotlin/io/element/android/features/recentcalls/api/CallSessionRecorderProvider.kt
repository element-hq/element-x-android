/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.api

import io.element.android.libraries.matrix.api.core.SessionId

interface CallSessionRecorderProvider {
    fun register(sessionId: SessionId, recorder: CallSessionRecorder)
    fun unregister(sessionId: SessionId)
    fun get(sessionId: SessionId): CallSessionRecorder?
}
