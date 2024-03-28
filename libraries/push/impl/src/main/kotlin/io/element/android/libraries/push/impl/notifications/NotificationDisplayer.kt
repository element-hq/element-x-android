/*
 * Copyright (c) 2021 New Vector Ltd
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

package io.element.android.libraries.push.impl.notifications

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import io.element.android.libraries.di.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class NotificationDisplayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    fun showNotificationMessage(tag: String?, id: Int, notification: Notification): Boolean {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("Not allowed to notify.")
            return false
        }
        notificationManager.notify(tag, id, notification)
        return true
    }

    fun cancelNotificationMessage(tag: String?, id: Int) {
        notificationManager.cancel(tag, id)
    }

    fun cancelAllNotifications() {
        // Keep this try catch (reported by GA)
        try {
            notificationManager.cancelAll()
        } catch (e: Exception) {
            Timber.e(e, "## cancelAllNotifications() failed")
        }
    }

    fun displayDiagnosticNotification(notification: Notification): Boolean {
        return showNotificationMessage(
            tag = "DIAGNOSTIC",
            id = NOTIFICATION_ID_DIAGNOSTIC,
            notification = notification
        )
    }

    fun dismissDiagnosticNotification() {
        cancelNotificationMessage(
            tag = "DIAGNOSTIC",
            id = NOTIFICATION_ID_DIAGNOSTIC
        )
    }

    /**
     * Cancel the foreground notification service.
     */
    fun cancelNotificationForegroundService() {
        notificationManager.cancel(NOTIFICATION_ID_FOREGROUND_SERVICE)
    }

    companion object {
        /* ==========================================================================================
         * IDs for notifications
         * ========================================================================================== */

        /**
         * Identifier of the foreground notification used to keep the application alive
         * when it runs in background.
         * This notification, which is not removable by the end user, displays what
         * the application is doing while in background.
         */
        private const val NOTIFICATION_ID_FOREGROUND_SERVICE = 61

        private const val NOTIFICATION_ID_DIAGNOSTIC = 888
    }
}
