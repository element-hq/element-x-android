/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.call.impl.notifications

/*
 * Copyright (c) 2024 New Vector Ltd
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

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import io.element.android.appconfig.ElementCallConfig
import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.services.DeclineCallBroadcastReceiver
import io.element.android.features.call.impl.utils.IntentProvider
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class RingingCallNotificationCreator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val matrixClientProvider: MatrixClientProvider,
    private val imageLoaderHolder: ImageLoaderHolder,
    private val notificationBitmapLoader: NotificationBitmapLoader,
) {
    suspend fun createNotification(
        sessionId: SessionId,
        roomId: RoomId,
        senderDisplayName: String,
        avatarUrl: String?,
        notificationChannelId: String,
        timestamp: Long,
    ): Notification? {
        val matrixClient = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return null
        val imageLoader = imageLoaderHolder.get(matrixClient)
        val largeIcon = notificationBitmapLoader.getUserIcon(avatarUrl, imageLoader)
        val caller = Person.Builder()
            .setName(senderDisplayName)
            .setIcon(largeIcon)
            .setImportant(true)
            .build()
        val answerIntent = IntentProvider.getPendingIntent(context, CallType.RoomCall(sessionId, roomId))
        // TODO: user right request codes
        val declineIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, DeclineCallBroadcastReceiver::class.java),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        // TODO: make this open the call
        return NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(CommonDrawables.ic_notification_small)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setStyle(NotificationCompat.CallStyle.forIncomingCall(caller, declineIntent, answerIntent).setIsVideo(true))
            .addPerson(caller)
            .setAutoCancel(true)
            .setWhen(timestamp)
            .setOngoing(true)
            .setShowWhen(false)
            .setTimeoutAfter(ElementCallConfig.RINGING_CALL_DURATION_SECONDS.seconds.inWholeMilliseconds)
            .setContentIntent(answerIntent)
            .setFullScreenIntent(answerIntent, true)
            .build()
            .apply {
                flags = flags.or(Notification.FLAG_INSISTENT)
            }
    }
}
