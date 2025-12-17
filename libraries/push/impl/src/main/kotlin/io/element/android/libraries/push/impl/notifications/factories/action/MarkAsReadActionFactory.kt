/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.factories.action

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dev.zacsweers.metro.Inject
import io.element.android.appconfig.NotificationConfig
import io.element.android.libraries.androidutils.uri.createIgnoredUri
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.NotificationActionIds
import io.element.android.libraries.push.impl.notifications.NotificationBroadcastReceiver
import io.element.android.libraries.push.impl.notifications.RoomEventGroupInfo
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock

@Inject
class MarkAsReadActionFactory(
    @ApplicationContext private val context: Context,
    private val actionIds: NotificationActionIds,
    private val stringProvider: StringProvider,
    private val clock: SystemClock,
) {
    fun create(roomInfo: RoomEventGroupInfo, threadId: ThreadId?): NotificationCompat.Action? {
        if (!NotificationConfig.SHOW_MARK_AS_READ_ACTION) return null
        val sessionId = roomInfo.sessionId.value
        val roomId = roomInfo.roomId.value
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = actionIds.markRoomRead
        intent.data = createIgnoredUri("markRead/$sessionId/$roomId" + threadId?.let { "/$it" }.orEmpty())
        intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, sessionId)
        intent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomId)
        threadId?.let { intent.putExtra(NotificationBroadcastReceiver.KEY_THREAD_ID, threadId.value) }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            clock.epochMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            CompoundDrawables.ic_compound_mark_as_read,
            stringProvider.getString(R.string.notification_room_action_mark_as_read),
            pendingIntent
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
            .setShowsUserInterface(false)
            .build()
    }
}
