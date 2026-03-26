/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.widget

import android.content.Context
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.appnav.widget.RecentChatsWidgetService
import io.element.android.appnav.widget.WidgetRoomData
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
@Inject
class DefaultRecentChatsWidgetService(
    @ApplicationContext private val context: Context,
) : RecentChatsWidgetService {
    override suspend fun updateRecentChats(rooms: List<WidgetRoomData>) {
        val widgetInfoList = rooms.map { room ->
            RoomWidgetInfo(
                sessionId = room.sessionId,
                roomId = room.roomId,
                name = room.name,
                lastMessage = room.lastMessage,
                lastActivityTimestamp = room.lastActivityTimestamp,
                unreadCount = room.unreadCount,
                senderName = room.senderName,
                avatarUrl = room.avatarUrl,
            )
        }
        RecentChatsWidgetUpdater.updateWidget(context, widgetInfoList)
    }
}
