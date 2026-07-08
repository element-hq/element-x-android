/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications

import android.content.Intent
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

interface RoomNotificationSettingsIntentProvider {
    suspend fun getIntent(
        sessionId: SessionId,
        roomId: RoomId,
        roomDisplayName: String,
        isDm: Boolean,
    ): Intent
}
