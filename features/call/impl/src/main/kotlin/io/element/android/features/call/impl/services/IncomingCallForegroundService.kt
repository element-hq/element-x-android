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

package io.element.android.features.call.impl.services

import android.Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.IntentCompat
import io.element.android.appconfig.ElementCallConfig
import io.element.android.features.call.impl.di.CallBindings
import io.element.android.features.call.impl.notifications.RingingCallNotificationCreator
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class IncomingCallForegroundService : Service() {
    companion object {
        private const val NOTIFICATION_DATA = "NOTIFICATION_DATA"

        internal fun start(context: Context, callNotificationData: CallNotificationData) {
            val intent = Intent(context, IncomingCallForegroundService::class.java)
            intent.putExtra(NOTIFICATION_DATA, callNotificationData)
            context.startService(intent)
        }
    }

    @Inject
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var ringingCallNotificationCreator: RingingCallNotificationCreator

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        bindings<CallBindings>().inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationData = intent?.let { IntentCompat.getParcelableExtra(it, NOTIFICATION_DATA, CallNotificationData::class.java) }
        if (notificationData == null) {
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }
        coroutineScope.launch {
            val notification = ringingCallNotificationCreator.createNotification(
                sessionId = notificationData.sessionId,
                roomId = notificationData.roomId,
                senderDisplayName = notificationData.senderName ?: notificationData.senderId.value,
                avatarUrl = notificationData.avatarUrl,
                notificationChannelId = notificationData.notificationChannelId,
                timestamp = notificationData.timestamp
            ) ?: run {
                stopSelf()
                return@launch
            }

            // TODO: set right id
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
            } else {
                startForeground(1, notification)
            }
            // Wait for the call to end
            delay(ElementCallConfig.RINGING_CALL_DURATION_SECONDS.seconds)
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun stopService(name: Intent?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        return super.stopService(name)
    }
}

class DeclineCallBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.stopService(Intent(context, IncomingCallForegroundService::class.java))
    }
}

@Parcelize
internal data class CallNotificationData(
    val sessionId: SessionId,
    val roomId: RoomId,
    val senderId: UserId,
    val senderName: String?,
    val avatarUrl: String?,
    val notificationChannelId: String,
    val timestamp: Long,
) : Parcelable
