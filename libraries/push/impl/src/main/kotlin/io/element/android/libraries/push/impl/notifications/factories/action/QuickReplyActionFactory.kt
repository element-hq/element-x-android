/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        return buildQuickReplyIntent(sessionId, roomId, threadId)?.let { replyPendingIntent ->
            val remoteInput = RemoteInput.Builder(NotificationBroadcastReceiver.KEY_TEXT_REPLY)
                .setLabel(stringProvider.getString(R.string.notification_room_action_quick_reply))
                .build()

            NotificationCompat.Action.Builder(
                R.drawable.vector_notification_quick_reply,
                stringProvider.getString(R.string.notification_room_action_quick_reply),
                replyPendingIntent
            )
                .addRemoteInput(remoteInput)
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                .setShowsUserInterface(false)
                .build()
        }
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
    ): PendingIntent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(context, NotificationBroadcastReceiver::class.java)
            intent.action = actionIds.smartReply
            intent.data = createIgnoredUri("quickReply/$sessionId/$roomId" + threadId?.let { "/$it" }.orEmpty())
            intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, sessionId.value)
            intent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomId.value)
            threadId?.let {
                intent.putExtra(NotificationBroadcastReceiver.KEY_THREAD_ID, it.value)
            }

            PendingIntent.getBroadcast(
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
        } else {
            null
        }
    }
}
