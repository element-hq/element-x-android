/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.push.impl.troubleshoot.NotificationClickHandler
import javax.inject.Inject

class TestNotificationReceiver : BroadcastReceiver() {
    @Inject lateinit var notificationClickHandler: NotificationClickHandler

    override fun onReceive(context: Context, intent: Intent) {
        context.bindings<TestNotificationReceiverBinding>().inject(this)
        notificationClickHandler.handleNotificationClick()
    }
}
