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

import android.annotation.SuppressLint
import androidx.core.app.NotificationManagerCompat
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.ElementCallConfig
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.notifications.RingingCallNotificationCreator
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.notifications.ForegroundServiceType
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.api.notifications.OnMissedCallNotificationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Manages the active call state.
 */
interface ActiveCallManager {
    /**
     * The active call state flow, which will be updated when the active call changes.
     */
    val activeCall: StateFlow<ActiveCall?>

    /**
     * Registers an incoming call if there isn't an existing active call and posts a [CallState.Ringing] notification.
     * @param notificationData The data for the incoming call notification.
     */
    fun registerIncomingCall(notificationData: CallNotificationData)

    /**
     * Called when the incoming call timed out. It will remove the active call and remove any associated UI, adding a 'missed call' notification.
     */
    fun incomingCallTimedOut()

    /**
     * Hangs up the active call and removes any associated UI.
     */
    fun hungUpCall()

    /**
     * Called when the user joins a call. It will remove any existing UI and set the call state as [CallState.InCall].
     *
     * @param sessionId The session ID of the user joining the call.
     * @param roomId The room ID of the call.
     */
    fun joinedCall(sessionId: SessionId, roomId: RoomId)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultActiveCallManager @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val matrixClientProvider: MatrixClientProvider,
    private val onMissedCallNotificationHandler: OnMissedCallNotificationHandler,
    private val ringingCallNotificationCreator: RingingCallNotificationCreator,
    private val notificationManagerCompat: NotificationManagerCompat,
) : ActiveCallManager {
    private var timedOutCallJob: Job? = null

    override val activeCall = MutableStateFlow<ActiveCall?>(null)

    override fun registerIncomingCall(notificationData: CallNotificationData) {
        if (activeCall.value != null) {
            displayMissedCallNotification(notificationData)
            Timber.w("Already have an active call, ignoring incoming call: $notificationData")
            return
        }
        activeCall.value = ActiveCall(
            sessionId = notificationData.sessionId,
            roomId = notificationData.roomId,
            callState = CallState.Ringing(notificationData),
        )

        timedOutCallJob = coroutineScope.launch {
            showIncomingCallNotification(notificationData)

            // Wait for the call to end
            delay(ElementCallConfig.RINGING_CALL_DURATION_SECONDS.seconds)
            incomingCallTimedOut()
        }
    }

    override fun incomingCallTimedOut() {
        val previousActiveCall = activeCall.value ?: return
        val notificationData = (previousActiveCall.callState as? CallState.Ringing)?.notificationData ?: return
        activeCall.value = null

        cancelIncomingCallNotification()

        displayMissedCallNotification(notificationData)
    }

    override fun hungUpCall() {
        cancelIncomingCallNotification()
        timedOutCallJob?.cancel()
        activeCall.value = null
    }

    override fun joinedCall(sessionId: SessionId, roomId: RoomId) {
        cancelIncomingCallNotification()
        timedOutCallJob?.cancel()

        activeCall.value = ActiveCall(
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

    @SuppressLint("MissingPermission")
    private suspend fun showIncomingCallNotification(notificationData: CallNotificationData) {
        val notification = ringingCallNotificationCreator.createNotification(
            sessionId = notificationData.sessionId,
            roomId = notificationData.roomId,
            eventId = notificationData.eventId,
            senderId = notificationData.senderId,
            roomName = notificationData.roomName,
            senderDisplayName = notificationData.senderName ?: notificationData.senderId.value,
            roomAvatarUrl = notificationData.avatarUrl,
            notificationChannelId = notificationData.notificationChannelId,
            timestamp = notificationData.timestamp
        ) ?: return
        runCatching {
            notificationManagerCompat.notify(
                NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.INCOMING_CALL),
                notification,
            )
        }.onFailure {
            Timber.e(it, "Failed to publish notification for incoming call")
        }
    }

    private fun cancelIncomingCallNotification() {
        notificationManagerCompat.cancel(NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.INCOMING_CALL))
    }

    private fun displayMissedCallNotification(notificationData: CallNotificationData) {
        coroutineScope.launch {
            onMissedCallNotificationHandler.addMissedCallNotification(
                sessionId = notificationData.sessionId,
                roomId = notificationData.roomId,
                eventId = notificationData.eventId,
            )
        }
    }
}

/**
 * Represents an active call.
 */
data class ActiveCall(
    val sessionId: SessionId,
    val roomId: RoomId,
    val callState: CallState,
)

/**
 * Represents the state of an active call.
 */
sealed interface CallState {
    /**
     * The call is in a ringing state.
     * @param notificationData The data for the incoming call notification.
     */
    data class Ringing(val notificationData: CallNotificationData) : CallState

    /**
     * The call is in an in-call state.
     */
    data object InCall : CallState
}
