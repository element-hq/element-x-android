/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.factories

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import io.element.android.libraries.androidutils.uri.createIgnoredUri
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.push.impl.intent.IntentProvider
import io.element.android.libraries.push.impl.notifications.NotificationActionIds
import io.element.android.libraries.push.impl.notifications.NotificationBroadcastReceiver
import io.element.android.libraries.push.impl.notifications.RoomEventGroupInfo
import io.element.android.libraries.push.impl.notifications.TestNotificationReceiver
import io.element.android.services.toolbox.api.systemclock.SystemClock
import javax.inject.Inject

class PendingIntentFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intentProvider: IntentProvider,
    private val clock: SystemClock,
    private val actionIds: NotificationActionIds,
) {
    fun createOpenSessionPendingIntent(sessionId: SessionId): PendingIntent? {
        return createRoomPendingIntent(sessionId = sessionId, roomId = null, threadId = null)
    }

    fun createOpenRoomPendingIntent(sessionId: SessionId, roomId: RoomId): PendingIntent? {
        return createRoomPendingIntent(sessionId = sessionId, roomId = roomId, threadId = null)
    }

    fun createOpenThreadPendingIntent(roomInfo: RoomEventGroupInfo, threadId: ThreadId?): PendingIntent? {
        return createRoomPendingIntent(sessionId = roomInfo.sessionId, roomId = roomInfo.roomId, threadId = threadId)
    }

    private fun createRoomPendingIntent(sessionId: SessionId, roomId: RoomId?, threadId: ThreadId?): PendingIntent? {
        val intent = intentProvider.getViewRoomIntent(sessionId = sessionId, roomId = roomId, threadId = threadId)
        return PendingIntent.getActivity(
            context,
            clock.epochMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun createDismissSummaryPendingIntent(sessionId: SessionId): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = actionIds.dismissSummary
        intent.data = createIgnoredUri("deleteSummary/$sessionId")
        intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, sessionId.value)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun createDismissRoomPendingIntent(sessionId: SessionId, roomId: RoomId): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = actionIds.dismissRoom
        intent.data = createIgnoredUri("deleteRoom/$sessionId/$roomId")
        intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, sessionId.value)
        intent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomId.value)
        return PendingIntent.getBroadcast(
            context,
            clock.epochMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun createDismissInvitePendingIntent(sessionId: SessionId, roomId: RoomId): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = actionIds.dismissInvite
        intent.data = createIgnoredUri("deleteInvite/$sessionId/$roomId")
        intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, sessionId.value)
        intent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomId.value)
        return PendingIntent.getBroadcast(
            context,
            clock.epochMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun createDismissEventPendingIntent(sessionId: SessionId, roomId: RoomId, eventId: EventId): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = actionIds.dismissEvent
        intent.data = createIgnoredUri("deleteEvent/$sessionId/$roomId/$eventId")
        intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, sessionId.value)
        intent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomId.value)
        intent.putExtra(NotificationBroadcastReceiver.KEY_EVENT_ID, eventId.value)
        return PendingIntent.getBroadcast(
            context,
            clock.epochMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun createTestPendingIntent(): PendingIntent? {
        val testActionIntent = Intent(context, TestNotificationReceiver::class.java)
        testActionIntent.action = actionIds.diagnostic
        return PendingIntent.getBroadcast(
            context,
            0,
            testActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
