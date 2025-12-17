/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2021-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.ApplicationContext
import timber.log.Timber

interface NotificationDisplayer {
    fun showNotification(tag: String?, id: Int, notification: Notification): Boolean
    fun cancelNotification(tag: String?, id: Int)
    fun displayDiagnosticNotification(notification: Notification): Boolean
    fun dismissDiagnosticNotification()
    fun displayUnregistrationNotification(notification: Notification): Boolean
}

@ContributesBinding(AppScope::class)
class DefaultNotificationDisplayer(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManagerCompat
) : NotificationDisplayer {
    override fun showNotification(tag: String?, id: Int, notification: Notification): Boolean {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("Not allowed to notify.")
            return false
        }
        notificationManager.notify(tag, id, notification)
        Timber.d("Notifying with tag: $tag, id: $id")
        return true
    }

    override fun cancelNotification(tag: String?, id: Int) {
        notificationManager.cancel(tag, id)
    }

    override fun displayDiagnosticNotification(notification: Notification): Boolean {
        return showNotification(
            tag = TAG_DIAGNOSTIC,
            id = NOTIFICATION_ID_DIAGNOSTIC,
            notification = notification
        )
    }

    override fun dismissDiagnosticNotification() {
        cancelNotification(
            tag = TAG_DIAGNOSTIC,
            id = NOTIFICATION_ID_DIAGNOSTIC
        )
    }

    override fun displayUnregistrationNotification(notification: Notification): Boolean {
        return showNotification(
            tag = TAG_DIAGNOSTIC,
            id = NOTIFICATION_ID_UNREGISTRATION,
            notification = notification,
        )
    }

    companion object {
        private const val TAG_DIAGNOSTIC = "DIAGNOSTIC"

        /* ==========================================================================================
         * IDs for notifications
         * ========================================================================================== */
        private const val NOTIFICATION_ID_DIAGNOSTIC = 888
        private const val NOTIFICATION_ID_UNREGISTRATION = 889
    }
}
