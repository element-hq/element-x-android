/*
 * Copyright (c) 2023 New Vector Ltd
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

@file:Suppress("UNUSED_PARAMETER")

package io.element.android.libraries.push.impl.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.push.impl.notifications.factories.NotificationFactory
import timber.log.Timber
import javax.inject.Inject

class NotificationUtils @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationFactory: NotificationFactory,
) {

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
        const val NOTIFICATION_ID_FOREGROUND_SERVICE = 61
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    /**
     * Cancel the foreground notification service.
     */
    fun cancelNotificationForegroundService() {
        notificationManager.cancel(NOTIFICATION_ID_FOREGROUND_SERVICE)
    }

    /**
     * Cancel all the notification.
     */
    fun cancelAllNotifications() {
        // Keep this try catch (reported by GA)
        try {
            notificationManager.cancelAll()
        } catch (e: Exception) {
            Timber.e(e, "## cancelAllNotifications() failed")
        }
    }

    @SuppressLint("LaunchActivityFromNotification")
    fun displayDiagnosticNotification() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("Not allowed to notify.")
            return
        }
        notificationManager.notify(
            "DIAGNOSTIC",
            888,
            notificationFactory.createDiagnosticNotification()
        )
    }
}
