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
import io.element.android.features.call.impl.services.CallNotificationData
import io.element.android.features.call.impl.services.IncomingCallForegroundService
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.notifications.OnMissedCallNotificationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@SingleIn(AppScope::class)
class CallIntegrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val matrixClientProvider: MatrixClientProvider,
    private val onMissedCallNotificationHandler: OnMissedCallNotificationHandler,
) {
    private val _activeCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall: StateFlow<ActiveCall?> = _activeCall

    fun registerIncomingCall(notificationData: CallNotificationData) {
        if (activeCall.value != null) {
            Timber.w("Already have an active call, ignoring incoming call: $notificationData")
            return
        }
        _activeCall.value = ActiveCall(
            sessionId = notificationData.sessionId,
            roomId = notificationData.roomId,
            callState = CallState.Ringing(notificationData),
        )
        IncomingCallForegroundService.start(context, notificationData)
    }

    fun incomingCallTimedOut() {
        val previousActiveCall = activeCall.value ?: return
        val notificationData = (previousActiveCall.callState as? CallState.Ringing)?.notificationData ?: return
        _activeCall.value = null

        IncomingCallForegroundService.stop(context)

        onMissedCallNotificationHandler.addMissedCallNotification(
            sessionId = previousActiveCall.sessionId,
            roomId = previousActiveCall.roomId,
            eventId = notificationData.eventId,
            senderId = notificationData.senderId,
            roomName = notificationData.roomName,
            senderName = notificationData.senderName ?: notificationData.senderId.value,
            timestamp = notificationData.timestamp,
            avatarUrl = notificationData.avatarUrl,
        )
    }

    fun hungUpCall() {
        _activeCall.value = null
        IncomingCallForegroundService.stop(context)
    }

    fun joinedCall(sessionId: SessionId, roomId: RoomId) {
        IncomingCallForegroundService.stop(context)

        _activeCall.value = ActiveCall(
            sessionId = sessionId,
            roomId = roomId,
            callState = CallState.InCall,
        )
        // Send call notification to the room
        coroutineScope.launch {
            matrixClientProvider.getOrRestore(sessionId)
                .getOrNull()
                ?.getRoom(roomId)
                ?.sendCallNotificationIfNeeded()
        }
    }
}

data class ActiveCall(
    val sessionId: SessionId,
    val roomId: RoomId,
    val callState: CallState,
)

sealed interface CallState {
    data class Ringing(val notificationData: CallNotificationData) : CallState
    data object InCall : CallState
}
