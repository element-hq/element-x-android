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

package io.element.android.features.call.impl

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.PendingIntentCompat
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.call.impl.services.CallNotificationData
import io.element.android.features.call.impl.services.IncomingCallForegroundService
import io.element.android.features.call.impl.ui.ElementCallActivity
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.core.UserId
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultElementCallEntryPoint @Inject constructor(
    @ApplicationContext private val context: Context,
) : ElementCallEntryPoint {
    companion object {
        const val EXTRA_CALL_TYPE = "EXTRA_CALL_TYPE"
        const val REQUEST_CODE = 2255
    }

    override fun startCall(callType: CallType) {
        context.startActivity(createIntent(callType))
    }

    override fun getPendingIntent(callType: CallType): PendingIntent {
        return PendingIntentCompat.getActivity(context, REQUEST_CODE, createIntent(callType), PendingIntent.FLAG_UPDATE_CURRENT, false)!!
    }

    override fun startIncomingRingingCallService(
        callType: CallType.RoomCall,
        senderId: UserId,
        senderName: String?,
        avatarUrl: String?,
        timestamp: Long,
        notificationChannelId: String,
    ) {
        val incomingCallNotificationData = CallNotificationData(
            sessionId = callType.sessionId,
            roomId = callType.roomId,
            senderId = senderId,
            senderName = senderName,
            avatarUrl = avatarUrl,
            timestamp = timestamp,
            notificationChannelId = notificationChannelId,
        )
        IncomingCallForegroundService.start(context, incomingCallNotificationData)
    }

    private fun createIntent(callType: CallType): Intent = Intent(context, ElementCallActivity::class.java).apply {
        putExtra(EXTRA_CALL_TYPE, callType)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
    }
}
