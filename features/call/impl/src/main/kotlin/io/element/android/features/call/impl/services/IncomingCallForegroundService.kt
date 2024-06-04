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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.IntentCompat
import io.element.android.appconfig.ElementCallConfig
import io.element.android.features.call.impl.di.CallBindings
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.notifications.RingingCallNotificationCreator
import io.element.android.features.call.impl.utils.ActiveCallManager
import io.element.android.libraries.architecture.bindings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * A foreground service that shows a notification for an incoming call.
 */
class IncomingCallForegroundService : Service() {
    companion object {
        private const val NOTIFICATION_DATA = "NOTIFICATION_DATA"

        /**
         * Starts the service to handle an incoming call.
         */
        internal fun start(context: Context, callNotificationData: CallNotificationData) {
            val intent = Intent(context, IncomingCallForegroundService::class.java)
            intent.putExtra(NOTIFICATION_DATA, callNotificationData)
            context.startService(intent)
        }

        /**
         * Stops the service.
         */
        internal fun stop(context: Context) {
            context.stopService(Intent(context, IncomingCallForegroundService::class.java))
        }
    }

    @Inject
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var ringingCallNotificationCreator: RingingCallNotificationCreator

    @Inject
    lateinit var activeCallManager: ActiveCallManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private var timedOutCallJob: Job? = null

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
        timedOutCallJob = coroutineScope.launch {
            activeCallManager.registerIncomingCall(notificationData)
            showIncomingCallNotification(notificationData)

            // Wait for the call to end
            delay(ElementCallConfig.RINGING_CALL_DURATION_SECONDS.seconds)
            activeCallManager.incomingCallTimedOut()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun stopService(name: Intent?): Boolean {
        timedOutCallJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        return super.stopService(name)
    }

    private suspend fun showIncomingCallNotification(notificationData: CallNotificationData) {
        val notification = ringingCallNotificationCreator.createNotification(
            sessionId = notificationData.sessionId,
            roomId = notificationData.roomId,
            eventId = notificationData.eventId,
            senderId = notificationData.senderId,
            roomName = notificationData.roomName,
            senderDisplayName = notificationData.senderName ?: notificationData.senderId.value,
            avatarUrl = notificationData.avatarUrl,
            notificationChannelId = notificationData.notificationChannelId,
            timestamp = notificationData.timestamp
        ) ?: return
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
        } else {
            0
        }
        // TODO: set right id
        runCatching {
            ServiceCompat.startForeground(this, 1, notification, serviceType)
        }.onFailure {
            Timber.e(it, "Failed to start foreground service for incoming calls")
        }
    }
}
