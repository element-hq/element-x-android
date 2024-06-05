/*
 * Copyright (c) 2024 New Vector Ltd
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
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class NotificationChannelsTest {
    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `init - creates notification channels and migrates old ones`() {
        val notificationManager = mockk<NotificationManagerCompat>(relaxed = true) {
            every { notificationChannels } returns emptyList()
        }

        createNotificationChannels(notificationManager = notificationManager)

        verify { notificationManager.createNotificationChannel(any<NotificationChannelCompat>()) }
        verify { notificationManager.createNotificationChannel(any<NotificationChannel>()) }
        verify { notificationManager.deleteNotificationChannel(any<String>()) }
    }

    @Test
    fun `getChannelForIncomingCall - returns the right channel`() {
        val notificationChannels = createNotificationChannels()

        val ringingChannel = notificationChannels.getChannelForIncomingCall(ring = true)
        assertThat(ringingChannel).isEqualTo(RINGING_CALL_NOTIFICATION_CHANNEL_ID)

        val normalChannel = notificationChannels.getChannelForIncomingCall(ring = false)
        assertThat(normalChannel).isEqualTo(CALL_NOTIFICATION_CHANNEL_ID_V3)
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
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        notificationManager = notificationManager,
        stringProvider = FakeStringProvider(),
    )
}
