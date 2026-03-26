/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import io.element.android.appnav.widget.WidgetRoomData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RecentChatsWidgetUpdater {
    suspend fun updateWidget(context: Context, rooms: List<WidgetRoomData>) {
        val chats = rooms
            .sortedByDescending { it.unreadCount }
            .take(10)
            .map { room ->
                WidgetChatItem(
                    sessionId = room.sessionId,
                    roomId = room.roomId,
                    roomName = room.name,
                    avatarInitial = room.name.firstOrNull()?.uppercase() ?: "?",
                    lastMessage = room.lastMessage ?: "",
                    timestamp = formatTimestamp(room.lastActivityTimestamp),
                    unreadCount = room.unreadCount,
                    senderName = room.senderName,
                    avatarUrl = room.avatarUrl,
                    isFavorite = room.isFavorite,
                )
            }
        RecentChatsDataStore.saveChats(context, chats)
        RecentChatsWidget().updateAll(context)
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "now"
            diff < 3_600_000 -> "${diff / 60_000}m"
            diff < 86_400_000 -> "${diff / 3_600_000}h"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
