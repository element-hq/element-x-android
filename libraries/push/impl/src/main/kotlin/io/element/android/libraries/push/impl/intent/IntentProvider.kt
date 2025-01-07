/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.intent

import android.content.Intent
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId

interface IntentProvider {
    /**
     * Provide an intent to start the application on a room or thread.
     */
    fun getViewRoomIntent(
        sessionId: SessionId,
        roomId: RoomId?,
        threadId: ThreadId?,
    ): Intent
}
