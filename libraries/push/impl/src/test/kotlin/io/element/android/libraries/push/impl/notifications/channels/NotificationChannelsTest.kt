/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.channels

import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class NotificationChannelsTest {
    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `init - creates notification channels and migrates old ones`() {
        val notificationManager = mockk<NotificationManagerCompat>(relaxed = true) {
            every { notificationChannels } returns emptyList()
        }

        createNotificationChannels(notificationManager = notificationManager)

        verify { notificationManager.createNotificationChannel(any<NotificationChannelCompat>()) }
        verify { notificationManager.deleteNotificationChannel(any<String>()) }
    }

    @Test
    fun `getChannelForIncomingCall - returns the right channel`() {
        val notificationChannels = createNotificationChannels()

        val ringingChannel = notificationChannels.getChannelForIncomingCall(ring = true)
        assertThat(ringingChannel).isEqualTo(RINGING_CALL_NOTIFICATION_CHANNEL_ID)

        val normalChannel = notificationChannels.getChannelForIncomingCall(ring = false)
        assertThat(normalChannel).isEqualTo(CALL_NOTIFICATION_CHANNEL_ID)
    }

    @Test
    fun `getChannelIdForMessage - returns the right channel`() {
        val notificationChannels = createNotificationChannels()

        assertThat(notificationChannels.getChannelIdForMessage(noisy = true)).isEqualTo(NOISY_NOTIFICATION_CHANNEL_ID)
        assertThat(notificationChannels.getChannelIdForMessage(noisy = false)).isEqualTo(SILENT_NOTIFICATION_CHANNEL_ID)
    }

    @Test
    fun `getChannelIdForTest - returns the right channel`() {
        val notificationChannels = createNotificationChannels()

        assertThat(notificationChannels.getChannelIdForTest()).isEqualTo(NOISY_NOTIFICATION_CHANNEL_ID)
    }

    private fun createNotificationChannels(
        notificationManager: NotificationManagerCompat = mockk(relaxed = true),
    ) = DefaultNotificationChannels(
        notificationManager = notificationManager,
        stringProvider = FakeStringProvider(),
        context = RuntimeEnvironment.getApplication(),
    )
}
