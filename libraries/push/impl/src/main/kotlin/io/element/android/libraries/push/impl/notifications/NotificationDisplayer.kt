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
