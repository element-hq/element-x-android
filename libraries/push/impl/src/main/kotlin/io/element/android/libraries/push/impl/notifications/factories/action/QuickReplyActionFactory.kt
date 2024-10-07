/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.factories.action

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import io.element.android.appconfig.NotificationConfig
import io.element.android.libraries.androidutils.uri.createIgnoredUri
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.NotificationActionIds
import io.element.android.libraries.push.impl.notifications.NotificationBroadcastReceiver
import io.element.android.libraries.push.impl.notifications.RoomEventGroupInfo
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import javax.inject.Inject

class QuickReplyActionFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val actionIds: NotificationActionIds,
    private val stringProvider: StringProvider,
    private val clock: SystemClock,
) {
    fun create(roomInfo: RoomEventGroupInfo, threadId: ThreadId?): NotificationCompat.Action? {
        if (!NotificationConfig.SUPPORT_QUICK_REPLY_ACTION) return null
        val sessionId = roomInfo.sessionId
        val roomId = roomInfo.roomId
        val replyPendingIntent = buildQuickReplyIntent(sessionId, roomId, threadId)
        val remoteInput = RemoteInput.Builder(NotificationBroadcastReceiver.KEY_TEXT_REPLY)
            .setLabel(stringProvider.getString(R.string.notification_room_action_quick_reply))
            .build()

        return NotificationCompat.Action.Builder(
            R.drawable.vector_notification_quick_reply,
            stringProvider.getString(R.string.notification_room_action_quick_reply),
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .setShowsUserInterface(false)
            .build()
    }

    /*
     * Direct reply is new in Android N, and Android already handles the UI, so the right pending intent
     * here will ideally be a Service/IntentService (for a long running background task) or a BroadcastReceiver,
     * which runs on the UI thread. It also works without unlocking, making the process really fluid for the user.
     * However, for Android devices running Marshmallow and below (API level 23 and below),
     * it will be more appropriate to use an activity. Since you have to provide your own UI.
     */
    private fun buildQuickReplyIntent(
        sessionId: SessionId,
        roomId: RoomId,
        threadId: ThreadId?,
    ): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = actionIds.smartReply
        intent.data = createIgnoredUri("quickReply/$sessionId/$roomId" + threadId?.let { "/$it" }.orEmpty())
        intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, sessionId.value)
        intent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomId.value)
        threadId?.let {
            intent.putExtra(NotificationBroadcastReceiver.KEY_THREAD_ID, it.value)
        }

        return PendingIntent.getBroadcast(
            context,
            clock.epochMillis().toInt(),
            intent,
            // PendingIntents attached to actions with remote inputs must be mutable
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else {
                0
            }
        )
    }
}
