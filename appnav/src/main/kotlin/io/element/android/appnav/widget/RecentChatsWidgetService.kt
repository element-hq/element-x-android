/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.widget

/**
 * Bridge interface for pushing room list data to the Recent Chats home-screen widget.
 *
 * Defined here in `appnav` so that [io.element.android.appnav.LoggedInFlowNode] can inject it
 * without depending on the `app` module. The `app` module provides the real implementation
 * (backed by [io.element.android.x.widget.RecentChatsWidgetUpdater]).
 */
interface RecentChatsWidgetService {
    /**
     * Push the latest room data to the widget. Implementations are expected to
     * persist the data and trigger a Glance widget refresh.
     */
    suspend fun updateRecentChats(rooms: List<WidgetRoomData>)
}

/**
 * Minimal room data needed by the widget. Deliberately kept simple to avoid
 * pulling Matrix API types into the widget layer.
 */
data class WidgetRoomData(
    val roomId: String,
    val name: String,
    val lastMessage: String?,
    val lastActivityTimestamp: Long,
    val unreadCount: Int,
)
