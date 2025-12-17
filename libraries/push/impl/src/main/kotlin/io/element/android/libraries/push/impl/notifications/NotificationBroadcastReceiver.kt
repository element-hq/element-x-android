/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.bindings

/**
 * Receives actions broadcast by notification (on click, on dismiss, inline replies, etc.).
 */
class NotificationBroadcastReceiver : BroadcastReceiver() {
    @Inject lateinit var notificationBroadcastReceiverHandler: NotificationBroadcastReceiverHandler

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return
        context.bindings<NotificationBroadcastReceiverBindings>().inject(this)
        notificationBroadcastReceiverHandler.onReceive(intent)
    }

    companion object {
        const val KEY_SESSION_ID = "sessionID"
        const val KEY_ROOM_ID = "roomID"
        const val KEY_THREAD_ID = "threadID"
        const val KEY_EVENT_ID = "eventID"
        const val KEY_TEXT_REPLY = "key_text_reply"
    }
}
