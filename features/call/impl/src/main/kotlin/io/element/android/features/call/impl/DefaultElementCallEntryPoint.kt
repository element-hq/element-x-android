/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.utils.ActiveCallManager
import io.element.android.features.call.impl.utils.IntentProvider
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultElementCallEntryPoint @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activeCallManager: ActiveCallManager,
) : ElementCallEntryPoint {
    companion object {
        const val EXTRA_CALL_TYPE = "EXTRA_CALL_TYPE"
        const val REQUEST_CODE = 2255
    }

    override fun startCall(callType: CallType) {
        context.startActivity(IntentProvider.createIntent(context, callType))
    }

    override suspend fun handleIncomingCall(
        callType: CallType.RoomCall,
        eventId: EventId,
        senderId: UserId,
        roomName: String?,
        senderName: String?,
        avatarUrl: String?,
        timestamp: Long,
        notificationChannelId: String,
        textContent: String?,
    ) {
        val incomingCallNotificationData = CallNotificationData(
            sessionId = callType.sessionId,
            roomId = callType.roomId,
            eventId = eventId,
            senderId = senderId,
            roomName = roomName,
            senderName = senderName,
            avatarUrl = avatarUrl,
            timestamp = timestamp,
            notificationChannelId = notificationChannelId,
            textContent = textContent,
        )
        activeCallManager.registerIncomingCall(notificationData = incomingCallNotificationData)
    }
}
