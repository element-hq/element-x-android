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
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.rageshake.test.logs.FakeAnnouncementService
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.NotificationSound
import io.element.android.libraries.preferences.api.store.NotificationSoundUnavailableState
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.libraries.push.api.notifications.SoundDisplayNameResolver
import io.element.android.libraries.push.test.notifications.FakeSoundDisplayNameResolver
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
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

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `recreate noisy then ringing produces both versioned channels with current ids`() {
        val notificationManager = mockk<NotificationManagerCompat>(relaxed = true) {
            every { notificationChannels } returns emptyList()
        }
        val channels = createNotificationChannels(notificationManager = notificationManager)

        channels.recreateNoisyChannel(sound = NotificationSound.Custom("content://a"), version = 5)
        channels.recreateRingingCallChannel(sound = NotificationSound.Custom("content://b"), version = 7)

        // Each recreate updates the corresponding currentId independently — the lock guarding
        // recreate* against itself does not interfere across the two channel families.
        assertThat(channels.getChannelIdForTest()).isEqualTo("${NOISY_NOTIFICATION_CHANNEL_ID_BASE}_v5")
        assertThat(channels.getChannelForIncomingCall(ring = true)).isEqualTo("${RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE}_v7")
        verify { notificationManager.createNotificationChannel(match<NotificationChannelCompat> { it.id == "${NOISY_NOTIFICATION_CHANNEL_ID_BASE}_v5" }) }
        verify {
            notificationManager.createNotificationChannel(
            match<NotificationChannelCompat> { it.id == "${RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE}_v7" }
        )
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `init - resolvable Custom sounds leave store untouched and do not announce`() = runTest {
        val store = InMemoryAppPreferencesStore(
            messageSound = NotificationSound.Custom("content://media/internal/audio/media/42"),
            callRingtone = NotificationSound.Custom("content://settings/system/ringtone"),
        )
        val announcementShown = mutableListOf<Announcement>()
        createNotificationChannels(
            appPreferencesStore = store,
            soundDisplayNameResolver = FakeSoundDisplayNameResolver(resolveLambda = { "Resolved" }),
            announcementService = FakeAnnouncementService(showAnnouncementResult = { announcementShown += it }),
        )

        assertThat(store.getNotificationSoundChannelConfig().messageSound)
            .isEqualTo(NotificationSound.Custom("content://media/internal/audio/media/42"))
        assertThat(store.getNotificationSoundChannelConfig().callRingtone)
            .isEqualTo(NotificationSound.Custom("content://settings/system/ringtone"))
        assertThat(store.getNotificationSoundUnavailableStateFlow().first()).isEqualTo(NotificationSoundUnavailableState.None)
        assertThat(announcementShown).isEmpty()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `init - unresolvable Custom message sound is reverted to SystemDefault and announces`() = runTest {
        val store = InMemoryAppPreferencesStore(
            messageSound = NotificationSound.Custom("content://gone/forever"),
            messageSoundChannelVersion = 3,
        )
        val announcementShown = mutableListOf<Announcement>()
        createNotificationChannels(
            appPreferencesStore = store,
            soundDisplayNameResolver = FakeSoundDisplayNameResolver(resolveLambda = { null }),
            announcementService = FakeAnnouncementService(showAnnouncementResult = { announcementShown += it }),
        )

        assertThat(store.getNotificationSoundChannelConfig().messageSound).isEqualTo(NotificationSound.SystemDefault)
        assertThat(store.getNotificationSoundChannelConfig().messageSoundVersion).isEqualTo(4)
        assertThat(store.getNotificationSoundUnavailableStateFlow().first()).isEqualTo(NotificationSoundUnavailableState.MessageSound)
        assertThat(announcementShown).containsExactly(Announcement.SoundUnavailable)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `init - both Custom sounds unresolvable - both reverted and announced once`() = runTest {
        val store = InMemoryAppPreferencesStore(
            messageSound = NotificationSound.Custom("content://gone/msg"),
            callRingtone = NotificationSound.Custom("content://gone/call"),
        )
        val announcementShown = mutableListOf<Announcement>()
        createNotificationChannels(
            appPreferencesStore = store,
            soundDisplayNameResolver = FakeSoundDisplayNameResolver(resolveLambda = { null }),
            announcementService = FakeAnnouncementService(showAnnouncementResult = { announcementShown += it }),
        )

        assertThat(store.getNotificationSoundChannelConfig().messageSound).isEqualTo(NotificationSound.SystemDefault)
        assertThat(store.getNotificationSoundChannelConfig().callRingtone).isEqualTo(NotificationSound.SystemDefault)
        assertThat(store.getNotificationSoundUnavailableStateFlow().first()).isEqualTo(NotificationSoundUnavailableState.Both)
        assertThat(announcementShown).containsExactly(Announcement.SoundUnavailable)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `init - URI reset failure does not strand banner-storm flag set against broken URI`() = runTest {
        // Simulate a DataStore that successfully reads the broken URI but throws when asked
        // to overwrite it with SystemDefault. Sanitization must NOT then set the unavailable
        // flag — otherwise next boot would re-detect, retry, and storm forever.
        val backing = InMemoryAppPreferencesStore(
            messageSound = NotificationSound.Custom("content://gone/forever"),
        )
        val store = object : AppPreferencesStore by backing {
            override suspend fun setMessageSoundAndIncrementVersion(sound: NotificationSound): Int {
                error("Simulated DataStore failure")
            }
        }
        val announcementShown = mutableListOf<Announcement>()
        createNotificationChannels(
            appPreferencesStore = store,
            soundDisplayNameResolver = FakeSoundDisplayNameResolver(resolveLambda = { null }),
            announcementService = FakeAnnouncementService(showAnnouncementResult = { announcementShown += it }),
        )

        // The unavailable state must remain None because the URI reset failed first.
        assertThat(store.getNotificationSoundUnavailableStateFlow().first()).isEqualTo(NotificationSoundUnavailableState.None)
        assertThat(announcementShown).isEmpty()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `init - state write failure leaves URIs reset but no banner state and no announcement`() = runTest {
        // The state write is one atomic call now. If it throws, everything below it (including
        // showAnnouncement) is skipped by runCatchingExceptions. URIs and channels were already
        // reset before the failing write, so on next boot sanitization is a no-op (the URIs are
        // already SystemDefault) and no banner ever appears for this incident. Acceptable: the
        // user just doesn't learn that their sound was reset, and they'll discover it the next
        // time they look at notification settings.
        val backing = InMemoryAppPreferencesStore(
            messageSound = NotificationSound.Custom("content://gone/msg"),
            callRingtone = NotificationSound.Custom("content://gone/call"),
        )
        val store = object : AppPreferencesStore by backing {
            override suspend fun setNotificationSoundUnavailableState(state: NotificationSoundUnavailableState) {
                error("Simulated DataStore failure on state write")
            }
        }
        val announcementShown = mutableListOf<Announcement>()
        val notificationManager = mockk<NotificationManagerCompat>(relaxed = true) {
            every { notificationChannels } returns emptyList()
        }
        createNotificationChannels(
            notificationManager = notificationManager,
            appPreferencesStore = store,
            soundDisplayNameResolver = FakeSoundDisplayNameResolver(resolveLambda = { null }),
            announcementService = FakeAnnouncementService(showAnnouncementResult = { announcementShown += it }),
        )

        assertThat(backing.getNotificationSoundChannelConfig().messageSound).isEqualTo(NotificationSound.SystemDefault)
        assertThat(backing.getNotificationSoundChannelConfig().callRingtone).isEqualTo(NotificationSound.SystemDefault)
        assertThat(backing.getNotificationSoundUnavailableStateFlow().first()).isEqualTo(NotificationSoundUnavailableState.None)
        assertThat(announcementShown).isEmpty()
        verify { notificationManager.createNotificationChannel(match<NotificationChannelCompat> { it.id == "${NOISY_NOTIFICATION_CHANNEL_ID_BASE}_v1" }) }
        verify {
            notificationManager.createNotificationChannel(
            match<NotificationChannelCompat> { it.id == "${RINGING_CALL_NOTIFICATION_CHANNEL_ID_BASE}_v1" }
        )
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `init - SystemDefault and Silent are not probed and never marked unavailable`() = runTest {
        val store = InMemoryAppPreferencesStore(
            messageSound = NotificationSound.SystemDefault,
            callRingtone = NotificationSound.Silent,
        )
        val announcementShown = mutableListOf<Announcement>()
        createNotificationChannels(
            appPreferencesStore = store,
            // A resolver that returns null would mark every Custom URI as unavailable — none here, so no effect.
            soundDisplayNameResolver = FakeSoundDisplayNameResolver(resolveLambda = { null }),
            announcementService = FakeAnnouncementService(showAnnouncementResult = { announcementShown += it }),
        )

        assertThat(store.getNotificationSoundUnavailableStateFlow().first()).isEqualTo(NotificationSoundUnavailableState.None)
        assertThat(announcementShown).isEmpty()
    }

    private fun createNotificationChannels(
        notificationManager: NotificationManagerCompat = mockk(relaxed = true),
        enterpriseService: EnterpriseService = FakeEnterpriseService(),
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
        soundDisplayNameResolver: SoundDisplayNameResolver = FakeSoundDisplayNameResolver(
            resolveLambda = { "Some sound" },
        ),
        announcementService: AnnouncementService = FakeAnnouncementService(
            showAnnouncementResult = {},
        ),
        // UnconfinedTestDispatcher makes launches run inline, mirroring the previous
        // runBlocking behavior so existing tests keep their synchronous shape.
        appCoroutineScope: CoroutineScope = TestScope(UnconfinedTestDispatcher()),
    ) = DefaultNotificationChannels(
        notificationManager = notificationManager,
        stringProvider = FakeStringProvider(),
        context = RuntimeEnvironment.getApplication(),
        enterpriseService = enterpriseService,
        appPreferencesStore = appPreferencesStore,
        soundDisplayNameResolver = soundDisplayNameResolver,
        announcementService = announcementService,
        appCoroutineScope = appCoroutineScope,
    )
}
