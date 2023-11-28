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

package io.element.android.libraries.push.impl.notifications.channels

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.push.impl.R
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

/**
 * on devices >= android O, we need to define a channel for each notifications.
 */
@SingleIn(AppScope::class)
class NotificationChannels @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stringProvider: StringProvider,
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    /* ==========================================================================================
     * Channel names
     * ========================================================================================== */

    /**
     * Create notification channels.
     */
    private fun createNotificationChannels() {
        if (!supportNotificationChannels()) {
            return
        }

        val accentColor = ContextCompat.getColor(context, R.color.notification_accent_color)

        // Migration - the noisy channel was deleted and recreated when sound preference was changed (id was DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID_BASE
        // + currentTimeMillis).
        // Now the sound can only be change directly in system settings, so for app upgrading we are deleting this former channel
        // Starting from this version the channel will not be dynamic
        for (channel in notificationManager.notificationChannels) {
            val channelId = channel.id
            val legacyBaseName = "DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID_BASE"
            if (channelId.startsWith(legacyBaseName)) {
                notificationManager.deleteNotificationChannel(channelId)
            }
        }
        // Migration - Remove deprecated channels
        for (channelId in listOf("DEFAULT_SILENT_NOTIFICATION_CHANNEL_ID", "CALL_NOTIFICATION_CHANNEL_ID")) {
            notificationManager.getNotificationChannel(channelId)?.let {
                notificationManager.deleteNotificationChannel(channelId)
            }
        }

        /**
         * Default notification importance: shows everywhere, makes noise, but does not visually
         * intrude.
         */
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOISY_NOTIFICATION_CHANNEL_ID,
                stringProvider.getString(R.string.notification_channel_noisy).ifEmpty { "Noisy notifications" },
                NotificationManager.IMPORTANCE_DEFAULT
            )
                .apply {
                    description = stringProvider.getString(R.string.notification_channel_noisy)
                    enableVibration(true)
                    enableLights(true)
                    lightColor = accentColor
                })

        /**
         * Low notification importance: shows everywhere, but is not intrusive.
         */
        notificationManager.createNotificationChannel(
            NotificationChannel(
                SILENT_NOTIFICATION_CHANNEL_ID,
                stringProvider.getString(R.string.notification_channel_silent).ifEmpty { "Silent notifications" },
                NotificationManager.IMPORTANCE_LOW
            )
                .apply {
                    description = stringProvider.getString(R.string.notification_channel_silent)
                    setSound(null, null)
                    enableLights(true)
                    lightColor = accentColor
                })

        notificationManager.createNotificationChannel(
            NotificationChannel(
                LISTENING_FOR_EVENTS_NOTIFICATION_CHANNEL_ID,
                stringProvider.getString(R.string.notification_channel_listening_for_events).ifEmpty { "Listening for events" },
                NotificationManager.IMPORTANCE_MIN
            )
                .apply {
                    description = stringProvider.getString(R.string.notification_channel_listening_for_events)
                    setSound(null, null)
                    setShowBadge(false)
                })

        notificationManager.createNotificationChannel(
            NotificationChannel(
                CALL_NOTIFICATION_CHANNEL_ID,
                stringProvider.getString(R.string.notification_channel_call).ifEmpty { "Call" },
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    description = stringProvider.getString(R.string.notification_channel_call)
                    setSound(null, null)
                    enableLights(true)
                    lightColor = accentColor
                })
    }

    private fun getChannel(channelId: String): NotificationChannel? {
        return notificationManager.getNotificationChannel(channelId)
    }

    fun getChannelForIncomingCall(fromBg: Boolean): NotificationChannel? {
        val notificationChannel = if (fromBg) CALL_NOTIFICATION_CHANNEL_ID else SILENT_NOTIFICATION_CHANNEL_ID
        return getChannel(notificationChannel)
    }

    fun getChannelIdForMessage(noisy: Boolean): String {
        return if (noisy) NOISY_NOTIFICATION_CHANNEL_ID else SILENT_NOTIFICATION_CHANNEL_ID
    }

    fun getChannelIdForTest(): String = NOISY_NOTIFICATION_CHANNEL_ID

    companion object {
        /* ==========================================================================================
         * IDs for channels
         * ========================================================================================== */
        private const val LISTENING_FOR_EVENTS_NOTIFICATION_CHANNEL_ID = "LISTEN_FOR_EVENTS_NOTIFICATION_CHANNEL_ID"
        private const val SILENT_NOTIFICATION_CHANNEL_ID = "DEFAULT_SILENT_NOTIFICATION_CHANNEL_ID_V2"
        private const val NOISY_NOTIFICATION_CHANNEL_ID = "DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID"
        private const val CALL_NOTIFICATION_CHANNEL_ID = "CALL_NOTIFICATION_CHANNEL_ID_V2"

        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
        private fun supportNotificationChannels() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}
