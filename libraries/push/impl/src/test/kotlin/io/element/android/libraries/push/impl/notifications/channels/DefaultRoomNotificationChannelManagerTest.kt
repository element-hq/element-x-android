/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.channels

import android.app.NotificationManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.lockscreen.test.FakeLockScreenService
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.test.FakeSessionPreferencesStoreFactory
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.libraries.push.impl.notifications.shortcut.createShortcutId
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class DefaultRoomNotificationChannelManagerTest : RobolectricTest() {
    private val sessionId = SessionId("@alice:example.org")
    private val roomA = RoomId("!roomA:example.org")
    private val roomB = RoomId("!roomB:example.org")
    private val notificationManager = NotificationManagerCompat.from(RuntimeEnvironment.getApplication())
    private val store = InMemorySessionPreferencesStore()

    private fun createManager(
        enterpriseService: FakeEnterpriseService = FakeEnterpriseService(getNoisyNotificationChannelIdResult = { null }),
        lockScreenService: FakeLockScreenService = FakeLockScreenService(),
        notificationChannels: FakeNotificationChannels = FakeNotificationChannels(
            channelIdForMessage = { _, noisy -> if (noisy) "SHARED_NOISY" else "SHARED_SILENT" },
        ),
    ) = DefaultRoomNotificationChannelManager(
        notificationManager = notificationManager,
        notificationChannels = notificationChannels,
        enterpriseService = enterpriseService,
        lockScreenService = lockScreenService,
        sessionPreferencesStoreFactory = FakeSessionPreferencesStoreFactory(getLambda = lambdaRecorder { _, _ -> store }),
        appCoroutineScope = MainScope(),
    )

    private fun channelIdFor(roomId: RoomId): String =
        "ROOM_NOTIFICATION_CHANNEL_${sessionId.value.hash().take(16)}_${roomId.value.hash().take(16)}"

    @Test
    fun `a silent notification for a room with no channel falls back to the shared silent channel`() = runTest {
        val manager = createManager()

        val channelId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = false)

        assertThat(channelId).isEqualTo("SHARED_SILENT")
    }

    @Test
    fun `a noisy notification for a room with no channel creates one`() = runTest {
        val manager = createManager()

        val channelId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)

        assertThat(channelId).isNotEqualTo("SHARED_NOISY")
        val channel = notificationManager.getNotificationChannel(channelId)
        assertThat(channel).isNotNull()
        assertThat(channel!!.name).isEqualTo("Room A")
        assertThat(channel.importance).isEqualTo(NotificationManager.IMPORTANCE_DEFAULT)
        assertThat(channel.conversationId).isEqualTo(createShortcutId(sessionId, roomA))
        assertThat(channel.parentChannelId).isEqualTo("SHARED_NOISY")
    }

    @Test
    fun `a noisy notification falls back to the shared noisy channel while PIN lock is enabled`() = runTest {
        val lockScreenService = FakeLockScreenService().apply { setIsPinSetup(true) }
        val manager = createManager(lockScreenService = lockScreenService)

        val channelId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)

        assertThat(channelId).isEqualTo("SHARED_NOISY")
        assertThat(notificationManager.notificationChannels.map { it.id }).doesNotContain(channelIdFor(roomA))
    }

    @Test
    fun `a room channel copies the current shared noisy channel sound`() = runTest {
        val parentChannelId = "SHARED_NOISY_CUSTOM"
        val parentSound = Uri.parse("content://example.test/custom-message-sound")
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(parentChannelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Noisy")
                .setSound(parentSound, null)
                .build()
        )
        val room = RoomId("!roomWithCustomSound:example.org")
        val manager = createManager(
            notificationChannels = FakeNotificationChannels(
                channelIdForMessage = { _, noisy -> if (noisy) parentChannelId else "SHARED_SILENT" },
            )
        )

        val channelId = manager.getChannelIdForRoom(sessionId, room, "Room with custom sound", isDm = false, noisy = true)

        assertThat(notificationManager.getNotificationChannel(channelId)!!.sound).isEqualTo(parentSound)
    }

    @Test
    fun `a room channel preserves a silent shared noisy channel`() = runTest {
        val parentChannelId = "SHARED_NOISY_SILENT"
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(parentChannelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Noisy")
                .setSound(null, null)
                .build()
        )
        val room = RoomId("!roomWithSilentSound:example.org")
        val manager = createManager(
            notificationChannels = FakeNotificationChannels(
                channelIdForMessage = { _, noisy -> if (noisy) parentChannelId else "SHARED_SILENT" },
            )
        )

        val channelId = manager.getChannelIdForRoom(sessionId, room, "Room with silent sound", isDm = false, noisy = true)

        assertThat(notificationManager.getNotificationChannel(channelId)!!.sound).isNull()
    }

    @Test
    fun `a DM room's channel is filed under the Private chats group`() = runTest {
        val manager = createManager()

        val channelId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = true, noisy = true)

        assertThat(notificationManager.getNotificationChannel(channelId)!!.group).isEqualTo(PRIVATE_CHATS_CHANNEL_GROUP_ID)
    }

    @Test
    fun `a non-DM room's channel is filed under the Rooms group`() = runTest {
        val manager = createManager()

        val channelId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)

        assertThat(notificationManager.getNotificationChannel(channelId)!!.group).isEqualTo(ROOMS_CHANNEL_GROUP_ID)
    }

    @Test
    fun `a channel is only created once, not on every notification`() = runTest {
        val manager = createManager()

        val firstId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)
        val secondId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)

        assertThat(secondId).isEqualTo(firstId)
    }

    @Test
    fun `a silent notification for a room with an existing channel still uses the shared silent channel`() = runTest {
        // Regression test: some push rule modes only bing on specific events (e.g. mentions) within
        // an otherwise-quiet room. A room's own channel must not swallow that distinction.
        val manager = createManager()
        manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)

        val channelId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = false)

        assertThat(channelId).isEqualTo("SHARED_SILENT")
    }

    @Test
    fun `enterprise override always wins over a per-room channel`() = runTest {
        // The manager delegates to notificationChannels.getChannelIdForMessage() once it has
        // already checked the enterprise override itself, so the fake must apply that same
        // override to mirror what the real NotificationChannels.getChannelIdForMessage does.
        val enterpriseService = FakeEnterpriseService(getNoisyNotificationChannelIdResult = { "MDM_CHANNEL" })
        val manager = createManager(
            enterpriseService = enterpriseService,
            notificationChannels = FakeNotificationChannels(
                channelIdForMessage = { sid, noisy -> if (noisy) enterpriseService.getNoisyNotificationChannelId(sid) ?: "SHARED_NOISY" else "SHARED_SILENT" },
            ),
        )

        val channelId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)

        assertThat(channelId).isEqualTo("MDM_CHANNEL")
    }

    @Test
    fun `clearRoomChannel deletes the channel and the persisted last-notified state`() = runTest {
        val manager = createManager()
        val channelId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)
        assertThat(notificationManager.getNotificationChannel(channelId)).isNotNull()

        manager.clearRoomChannel(sessionId, roomA)

        assertThat(notificationManager.getNotificationChannel(channelId)).isNull()
        assertThat(store.getRoomChannelLastNotifiedByHash()).isEmpty()
    }

    @Test
    fun `pruneChannelsForSession deletes channels for rooms no longer present`() = runTest {
        val manager = createManager()
        val idA = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)
        val idB = manager.getChannelIdForRoom(sessionId, roomB, "Room B", isDm = false, noisy = true)

        manager.pruneChannelsForSession(sessionId, roomIds = setOf(roomB))

        assertThat(notificationManager.getNotificationChannel(idA)).isNull()
        assertThat(notificationManager.getNotificationChannel(idB)).isNotNull()
        assertThat(store.getRoomChannelLastNotifiedByHash()).containsKey(idB.substringAfterLast("_"))
        assertThat(store.getRoomChannelLastNotifiedByHash()).doesNotContainKey(idA.substringAfterLast("_"))
    }

    @Test
    fun `clearAllChannelsForSession deletes every room channel for that session`() = runTest {
        val manager = createManager()
        val idA = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)
        val idB = manager.getChannelIdForRoom(sessionId, roomB, "Room B", isDm = false, noisy = true)

        manager.clearAllChannelsForSession(sessionId)

        assertThat(notificationManager.getNotificationChannel(idA)).isNull()
        assertThat(notificationManager.getNotificationChannel(idB)).isNull()
        assertThat(store.getRoomChannelLastNotifiedByHash()).isEmpty()
    }

    @Test
    fun `pruneInactiveChannels leaves a channel alone if its live settings were changed`() = runTest {
        val manager = createManager()
        val channelId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)
        // Simulate the user changing importance for this specific channel via system Settings.
        notificationManager.getNotificationChannel(channelId)!!.importance = NotificationManager.IMPORTANCE_LOW
        store.givenRoomChannelLastNotified(roomA, System.currentTimeMillis() - THIRTY_ONE_DAYS_MILLIS)

        manager.pruneInactiveChannels(sessionId)

        assertThat(notificationManager.getNotificationChannel(channelId)).isNotNull()
    }

    @Test
    fun `pruneInactiveChannels deletes an unmodified channel past the retention window`() = runTest {
        val manager = createManager()
        val channelId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)
        store.givenRoomChannelLastNotified(roomA, System.currentTimeMillis() - THIRTY_ONE_DAYS_MILLIS)

        manager.pruneInactiveChannels(sessionId)

        assertThat(notificationManager.getNotificationChannel(channelId)).isNull()
    }

    @Test
    fun `pruneInactiveChannels keeps a recently-notified channel`() = runTest {
        val manager = createManager()
        val channelId = manager.getChannelIdForRoom(sessionId, roomA, "Room A", isDm = false, noisy = true)

        manager.pruneInactiveChannels(sessionId)

        assertThat(notificationManager.getNotificationChannel(channelId)).isNotNull()
    }

    @Test
    fun `pruneInactiveChannels trims the oldest channels once over the count limit`() = runTest {
        val manager = createManager()
        val now = System.currentTimeMillis()
        val roomIds = (0 until MAX_CHANNELS + 2).map { RoomId("!room$it:example.org") }
        val channelIds = roomIds.mapIndexed { index, roomId ->
            val id = manager.getChannelIdForRoom(sessionId, roomId, "Room $index", isDm = false, noisy = true)
            // All well within the retention window, but with a clear oldest-first order to trim.
            store.givenRoomChannelLastNotified(roomId, now - (roomIds.size - index))
            id
        }

        manager.pruneInactiveChannels(sessionId)

        val remaining = channelIds.count { notificationManager.getNotificationChannel(it) != null }
        assertThat(remaining).isEqualTo(MAX_CHANNELS)
        // The two oldest-notified rooms (index 0 and 1) should be the ones trimmed.
        assertThat(notificationManager.getNotificationChannel(channelIds[0])).isNull()
        assertThat(notificationManager.getNotificationChannel(channelIds[1])).isNull()
        assertThat(notificationManager.getNotificationChannel(channelIds.last())).isNotNull()
    }

    private companion object {
        const val THIRTY_ONE_DAYS_MILLIS = 31L * 24 * 60 * 60 * 1000
        const val MAX_CHANNELS = 50
    }
}
