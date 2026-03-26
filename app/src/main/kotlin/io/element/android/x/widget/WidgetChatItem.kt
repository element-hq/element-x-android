/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.widget

import kotlinx.serialization.Serializable

@Serializable
data class WidgetChatItem(
    val sessionId: String = "",
    val roomId: String,
    val roomName: String,
    val avatarInitial: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int,
)
