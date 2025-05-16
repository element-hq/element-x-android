/*
 * Copyright 2021-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

interface NotificationDisplayer {
    fun showNotificationMessage(tag: String?, id: Int, notification: Notification): Boolean
    fun cancelNotificationMessage(tag: String?, id: Int)
    fun displayDiagnosticNotification(notification: Notification): Boolean
    fun dismissDiagnosticNotification()
}

@ContributesBinding(AppScope::class)
class DefaultNotificationDisplayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManagerCompat
) : NotificationDisplayer {
    override fun showNotificationMessage(tag: String?, id: Int, notification: Notification): Boolean {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("Not allowed to notify.")
            return false
        }
        notificationManager.notify(tag, id, notification)
        Timber.d("Notifying with tag: $tag, id: $id")
        return true
    }

    override fun cancelNotificationMessage(tag: String?, id: Int) {
        notificationManager.cancel(tag, id)
    }

    override fun displayDiagnosticNotification(notification: Notification): Boolean {
        return showNotificationMessage(
            tag = "DIAGNOSTIC",
            id = NOTIFICATION_ID_DIAGNOSTIC,
            notification = notification
        )
    }

    override fun dismissDiagnosticNotification() {
        cancelNotificationMessage(
            tag = "DIAGNOSTIC",
            id = NOTIFICATION_ID_DIAGNOSTIC
        )
    }

    companion object {
        /* ==========================================================================================
         * IDs for notifications
         * ========================================================================================== */
        private const val NOTIFICATION_ID_DIAGNOSTIC = 888
    }
}
