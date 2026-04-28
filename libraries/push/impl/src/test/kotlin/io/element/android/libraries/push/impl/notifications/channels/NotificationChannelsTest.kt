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
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.NotificationSound
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
        assertThat(ringingChannel).isEqualTo(RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE)

        val normalChannel = notificationChannels.getChannelForIncomingCall(ring = false)
        assertThat(normalChannel).isEqualTo(CALL_NOTIFICATION_CHANNEL_ID)
    }

    @Test
    fun `getChannelIdForMessage - returns the right channel`() {
        val notificationChannels = createNotificationChannels(
            enterpriseService = FakeEnterpriseService(
                getNoisyNotificationChannelIdResult = { null }
            ),
        )
        assertThat(notificationChannels.getChannelIdForMessage(sessionId = A_SESSION_ID, noisy = true))
            .isEqualTo(NOISY_NOTIFICATION_CHANNEL_ID_BASE)
        assertThat(notificationChannels.getChannelIdForMessage(sessionId = A_SESSION_ID, noisy = false))
            .isEqualTo(SILENT_NOTIFICATION_CHANNEL_ID)
    }

    @Test
    fun `getChannelIdForMessage - returns the right channel when enterprise service override the result`() {
        val notificationChannels = createNotificationChannels(
            enterpriseService = FakeEnterpriseService(
                getNoisyNotificationChannelIdResult = { "A_CHANNEL_ID" }
            ),
        )
        assertThat(notificationChannels.getChannelIdForMessage(sessionId = A_SESSION_ID, noisy = true))
            .isEqualTo("A_CHANNEL_ID")
        assertThat(notificationChannels.getChannelIdForMessage(sessionId = A_SESSION_ID, noisy = false))
            .isEqualTo(SILENT_NOTIFICATION_CHANNEL_ID)
    }

    @Test
    fun `getChannelIdForTest - returns the right channel`() {
        val notificationChannels = createNotificationChannels()

        assertThat(notificationChannels.getChannelIdForTest()).isEqualTo(NOISY_NOTIFICATION_CHANNEL_ID_BASE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `init - reads persisted message version and seeds versioned channel id`() {
        val notificationChannels = createNotificationChannels(
            appPreferencesStore = InMemoryAppPreferencesStore(messageSoundChannelVersion = 3),
            enterpriseService = FakeEnterpriseService(getNoisyNotificationChannelIdResult = { null }),
        )
        assertThat(notificationChannels.getChannelIdForTest()).isEqualTo("${NOISY_NOTIFICATION_CHANNEL_ID_BASE}_v3")
        assertThat(notificationChannels.getChannelIdForMessage(sessionId = A_SESSION_ID, noisy = true))
            .isEqualTo("${NOISY_NOTIFICATION_CHANNEL_ID_BASE}_v3")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `init - reads persisted call ringtone version and seeds versioned channel id`() {
        val notificationChannels = createNotificationChannels(
            appPreferencesStore = InMemoryAppPreferencesStore(callRingtoneChannelVersion = 2),
        )
        assertThat(notificationChannels.getChannelForIncomingCall(ring = true))
            .isEqualTo("${RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE}_v2")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `recreateNoisyChannel - creates new versioned channel and updates current id`() {
        val notificationManager = mockk<NotificationManagerCompat>(relaxed = true) {
            every { notificationChannels } returns emptyList()
        }
        val channels = createNotificationChannels(notificationManager = notificationManager)

        channels.recreateNoisyChannel(sound = NotificationSound.Custom("content://media/internal/audio/media/42"), version = 7)

        val expectedId = "${NOISY_NOTIFICATION_CHANNEL_ID_BASE}_v7"
        assertThat(channels.getChannelIdForTest()).isEqualTo(expectedId)
        verify { notificationManager.createNotificationChannel(match<NotificationChannelCompat> { it.id == expectedId }) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `recreateNoisyChannel - Silent produces channel with no sound`() {
        val captured = slot<NotificationChannelCompat>()
        val notificationManager = mockk<NotificationManagerCompat>(relaxed = true) {
            every { notificationChannels } returns emptyList()
            every { createNotificationChannel(capture(captured)) } returns Unit
        }
        val channels = createNotificationChannels(notificationManager = notificationManager)

        channels.recreateNoisyChannel(sound = NotificationSound.Silent, version = 1)

        // The last captured channel (the recreated one) should have a null sound and the new id.
        assertThat(captured.captured.id).isEqualTo("${NOISY_NOTIFICATION_CHANNEL_ID_BASE}_v1")
        assertThat(captured.captured.sound).isNull()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `recreateNoisyChannel - SystemDefault falls back to bundled default sound`() {
        val captured = slot<NotificationChannelCompat>()
        val notificationManager = mockk<NotificationManagerCompat>(relaxed = true) {
            every { notificationChannels } returns emptyList()
            every { createNotificationChannel(capture(captured)) } returns Unit
        }
        val channels = createNotificationChannels(notificationManager = notificationManager)

        channels.recreateNoisyChannel(sound = NotificationSound.SystemDefault, version = 1)

        assertThat(captured.captured.sound?.scheme).isEqualTo("android.resource")
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `recreateNoisyChannel - deletes prior versioned channels`() {
        val priorChannel = mockk<android.app.NotificationChannel>(relaxed = true) {
            every { id } returns "${NOISY_NOTIFICATION_CHANNEL_ID_BASE}_v3"
        }
        val notificationManager = mockk<NotificationManagerCompat>(relaxed = true) {
            every { notificationChannels } returns listOf(priorChannel)
        }
        val channels = createNotificationChannels(notificationManager = notificationManager)

        channels.recreateNoisyChannel(sound = NotificationSound.SystemDefault, version = 4)

        verify { notificationManager.deleteNotificationChannel("${NOISY_NOTIFICATION_CHANNEL_ID_BASE}_v3") }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `recreateRingingCallChannel - creates new versioned channel and updates current id`() {
        val notificationManager = mockk<NotificationManagerCompat>(relaxed = true) {
            every { notificationChannels } returns emptyList()
        }
        val channels = createNotificationChannels(notificationManager = notificationManager)

        channels.recreateRingingCallChannel(sound = NotificationSound.Custom("content://settings/system/ringtone"), version = 5)

        val expectedId = "${RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE}_v5"
        assertThat(channels.getChannelForIncomingCall(ring = true)).isEqualTo(expectedId)
        verify { notificationManager.createNotificationChannel(match<NotificationChannelCompat> { it.id == expectedId }) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `recreateRingingCallChannel - Silent produces channel with no sound`() {
        val captured = slot<NotificationChannelCompat>()
        val notificationManager = mockk<NotificationManagerCompat>(relaxed = true) {
            every { notificationChannels } returns emptyList()
            every { createNotificationChannel(capture(captured)) } returns Unit
        }
        val channels = createNotificationChannels(notificationManager = notificationManager)

        channels.recreateRingingCallChannel(sound = NotificationSound.Silent, version = 1)

        assertThat(captured.captured.id).isEqualTo("${RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE}_v1")
        assertThat(captured.captured.sound).isNull()
    }

    private fun createNotificationChannels(
        notificationManager: NotificationManagerCompat = mockk(relaxed = true),
        enterpriseService: EnterpriseService = FakeEnterpriseService(),
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
    ) = DefaultNotificationChannels(
        notificationManager = notificationManager,
        stringProvider = FakeStringProvider(),
        context = RuntimeEnvironment.getApplication(),
        enterpriseService = enterpriseService,
        appPreferencesStore = appPreferencesStore,
    )
}
