/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dev.zacsweers.metro.Inject
import io.element.android.libraries.di.annotations.ApplicationContext

@Inject
class LiveLocationSharingNotificationCreator(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "LIVE_LOCATION_SHARING"
    }

    fun createNotification(): Notification {
        if (supportNotificationChannels()) {
            ensureChannelExists()
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Sharing live location")
            .setContentText("Your live location is being shared")
            .setOngoing(true)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ensureChannelExists() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Live Location Sharing",
                    NotificationManager.IMPORTANCE_LOW,
                )
            )
        }
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
    private fun supportNotificationChannels() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}
