/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.shortcut

import androidx.core.content.pm.ShortcutInfoCompat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

internal fun createShortcutId(sessionId: SessionId, roomId: RoomId) = "$sessionId-$roomId"

internal fun Iterable<ShortcutInfoCompat>.filterBySession(sessionId: SessionId): Iterable<ShortcutInfoCompat> {
    val prefix = "$sessionId-"
    return filter { it.id.startsWith(prefix) }
}
