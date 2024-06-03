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

package io.element.android.features.call.impl.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.telecom.DisconnectCause
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.telecom.CallAttributesCompat
import androidx.core.telecom.CallsManager
import io.element.android.features.call.impl.notifications.RingingCallNotificationCreator
import io.element.android.features.call.impl.services.CallNotificationData
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@RequiresApi(Build.VERSION_CODES.O)
class CallIntegrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val ringingCallNotificationCreator: RingingCallNotificationCreator,
) {
    fun registerIncomingCall(notificationData: CallNotificationData) {
        val callsManager = CallsManager(context)
        callsManager.registerAppWithTelecom(CallsManager.CAPABILITY_SUPPORTS_VIDEO_CALLING)

        val callAttributes = CallAttributesCompat(
            displayName = notificationData.senderName ?: notificationData.senderId.value,
            address = Uri.EMPTY, // TODO: use real uri
            direction = CallAttributesCompat.DIRECTION_INCOMING,
            callType = CallAttributesCompat.CALL_TYPE_VIDEO_CALL,
            callCapabilities = 0,
        )
        coroutineScope.launch {
            callsManager.addCall(
                callAttributes = callAttributes,
                onAnswer = {},
                onDisconnect = {},
                onSetActive = {},
                onSetInactive = {}
            ) {
                coroutineScope.launch {
                    // Using the foreground service displays the notification, but it doesn't work as expected
//                    IncomingCallForegroundService.start(context, notificationData)

                    // Directly displaying the notification crashes the app with:
                    // "Not posted. CallStyle notifications must be for a foreground service or user initated job or use a fullScreenIntent."
                    // Which is weird, because the notification *does have* a fullScreenIntent
                    val notification = ringingCallNotificationCreator.createNotification(
                        sessionId = notificationData.sessionId,
                        roomId = notificationData.roomId,
                        senderDisplayName = notificationData.senderName ?: notificationData.senderId.value,
                        avatarUrl = notificationData.avatarUrl,
                        notificationChannelId = notificationData.notificationChannelId,
                        timestamp = notificationData.timestamp
                    )!!

                    NotificationManagerCompat.from(context).notify(54321, notification)

                    delay(5.seconds)
                    disconnect(DisconnectCause(DisconnectCause.LOCAL))
                }
            }
        }
    }
}
