/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
