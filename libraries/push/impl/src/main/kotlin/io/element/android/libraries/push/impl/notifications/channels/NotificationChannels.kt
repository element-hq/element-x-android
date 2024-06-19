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

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.NotificationConfig
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.push.impl.R
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

/* ==========================================================================================
 * IDs for channels
 * ========================================================================================== */
internal const val SILENT_NOTIFICATION_CHANNEL_ID = "DEFAULT_SILENT_NOTIFICATION_CHANNEL_ID_V2"
internal const val NOISY_NOTIFICATION_CHANNEL_ID = "DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID"
internal const val CALL_NOTIFICATION_CHANNEL_ID = "CALL_NOTIFICATION_CHANNEL_ID_V3"
internal const val RINGING_CALL_NOTIFICATION_CHANNEL_ID = "RINGING_CALL_NOTIFICATION_CHANNEL_ID"

/**
 * on devices >= android O, we need to define a channel for each notifications.
 */
interface NotificationChannels {
    /**
     * Get the channel for incoming call.
     * @param ring true if the device should ring when receiving the call.
     */
    fun getChannelForIncomingCall(ring: Boolean): String

    /**
     * Get the channel for messages.
     * @param noisy true if the notification should have sound and vibration.
     */
    fun getChannelIdForMessage(noisy: Boolean): String

    /**
     * Get the channel for test notifications.
     */
    fun getChannelIdForTest(): String
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
private fun supportNotificationChannels() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultNotificationChannels @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManagerCompat,
    private val stringProvider: StringProvider,
) : NotificationChannels {
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

        val accentColor = NotificationConfig.NOTIFICATION_ACCENT_COLOR

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
        for (channelId in listOf(
            "DEFAULT_SILENT_NOTIFICATION_CHANNEL_ID",
            "CALL_NOTIFICATION_CHANNEL_ID",
            "CALL_NOTIFICATION_CHANNEL_ID_V2",
            "LISTEN_FOR_EVENTS_NOTIFICATION_CHANNEL_ID",
        )) {
            notificationManager.getNotificationChannel(channelId)?.let {
                notificationManager.deleteNotificationChannel(channelId)
            }
        }

        /**
         * Default notification importance: shows everywhere, makes noise, but does not visually
         * intrude.
         */
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                NOISY_NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_DEFAULT
            )
                .setName(stringProvider.getString(R.string.notification_channel_noisy).ifEmpty { "Noisy notifications" })
                .setDescription(stringProvider.getString(R.string.notification_channel_noisy))
                .setVibrationEnabled(true)
                .setLightsEnabled(true)
                .setLightColor(accentColor)
                .build()
        )

        /**
         * Low notification importance: shows everywhere, but is not intrusive.
         */
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                SILENT_NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW
            )
                .setName(stringProvider.getString(R.string.notification_channel_silent).ifEmpty { "Silent notifications" })
                .setDescription(stringProvider.getString(R.string.notification_channel_silent))
                .setSound(null, null)
                .setLightsEnabled(true)
                .setLightColor(accentColor)
                .build()
        )

        // Register a channel for incoming and in progress call notifications with no ringing
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                CALL_NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_HIGH
            )
                .setName(stringProvider.getString(R.string.notification_channel_call).ifEmpty { "Call" })
                .setDescription(stringProvider.getString(R.string.notification_channel_call))
                .setVibrationEnabled(true)
                .setLightsEnabled(true)
                .setLightColor(accentColor)
                .build()
        )

        // Register a channel for incoming call notifications which will ring the device when received
        val ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                RINGING_CALL_NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_MAX,
            )
                .setName(stringProvider.getString(R.string.notification_channel_ringing_calls).ifEmpty { "Ringing calls" })
                .setVibrationEnabled(true)
                .setSound(
                    ringtoneUri,
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_RING)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build()
                )
                .setDescription(stringProvider.getString(R.string.notification_channel_ringing_calls))
                .setLightsEnabled(true)
                .setLightColor(accentColor)
                .build()
        )
    }

    override fun getChannelForIncomingCall(ring: Boolean): String {
        return if (ring) RINGING_CALL_NOTIFICATION_CHANNEL_ID else CALL_NOTIFICATION_CHANNEL_ID
    }

    override fun getChannelIdForMessage(noisy: Boolean): String {
        return if (noisy) NOISY_NOTIFICATION_CHANNEL_ID else SILENT_NOTIFICATION_CHANNEL_ID
    }

    override fun getChannelIdForTest(): String = NOISY_NOTIFICATION_CHANNEL_ID
}
