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

package io.element.android.features.call.impl.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.IntentCompat
import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.di.CallBindings
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.features.call.impl.utils.ActiveCallManager
import io.element.android.libraries.architecture.bindings
import javax.inject.Inject

/**
 * Broadcast receiver to decline the incoming call.
 */
class DeclineCallBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_NOTIFICATION_DATA = "EXTRA_NOTIFICATION_DATA"
    }
    @Inject
    lateinit var activeCallManager: ActiveCallManager
    override fun onReceive(context: Context, intent: Intent?) {
        val notificationData = intent?.let { IntentCompat.getParcelableExtra(it, EXTRA_NOTIFICATION_DATA, CallNotificationData::class.java) }
            ?: return
        context.bindings<CallBindings>().inject(this)
        activeCallManager.hungUpCall(callType = CallType.RoomCall(notificationData.sessionId, notificationData.roomId))
    }
}
