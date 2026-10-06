/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications.conversations

import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Build the id of the conversation shortcut for a room. The id is part of the contract with the system, which
 * hands it back in [android.content.Intent.EXTRA_SHORTCUT_ID] when the user shares to that shortcut.
 */
fun createConversationShortcutId(sessionId: SessionId, roomId: RoomId) = "$sessionId-$roomId"

/**
 * The room [shortcutId] was built for, or `null` when it is not the id of a conversation shortcut of [sessionId].
 */
fun conversationShortcutRoomId(shortcutId: String, sessionId: SessionId): RoomId? {
    val prefix = "$sessionId-"
    if (!shortcutId.startsWith(prefix)) return null
    // Another app can send any id in the share intent, so what follows the prefix is not necessarily a room id.
    return shortcutId.removePrefix(prefix).takeIf { MatrixPatterns.isRoomId(it) }?.let(::RoomId)
}
