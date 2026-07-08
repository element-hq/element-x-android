/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.channels

import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.notifications.shortcut.createShortcutId
import io.element.android.libraries.push.test.notifications.channels.FakeRoomNotificationChannelManager
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

class DefaultRoomNotificationSettingsIntentProviderTest : RobolectricTest() {
    private val context = RuntimeEnvironment.getApplication()
    private val sessionId = SessionId("@alice:example.org")
    private val roomId = RoomId("!room:example.org")

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `getIntent opens conversation notification settings on Android 11 and above`() = runTest {
        val provider = createProvider(channelId = "room-channel")

        val intent = provider.getIntent(sessionId, roomId, "Room", isDm = false)

        assertThat(intent.action).isEqualTo(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        assertThat(intent.getStringExtra(Settings.EXTRA_APP_PACKAGE)).isEqualTo(context.packageName)
        assertThat(intent.getStringExtra(Settings.EXTRA_CHANNEL_ID)).isEqualTo("room-channel")
        assertThat(intent.getStringExtra(Settings.EXTRA_CONVERSATION_ID)).isEqualTo(createShortcutId(sessionId, roomId))
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK).isEqualTo(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `getIntent opens channel notification settings on Android 8 to 10`() = runTest {
        val provider = createProvider(channelId = "room-channel")

        val intent = provider.getIntent(sessionId, roomId, "Room", isDm = false)

        assertThat(intent.action).isEqualTo(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        assertThat(intent.getStringExtra(Settings.EXTRA_APP_PACKAGE)).isEqualTo(context.packageName)
        assertThat(intent.getStringExtra(Settings.EXTRA_CHANNEL_ID)).isEqualTo("room-channel")
        assertThat(intent.hasExtra(Settings.EXTRA_CONVERSATION_ID)).isFalse()
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK).isEqualTo(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `getIntent falls back to app settings before notification channels`() = runTest {
        val provider = createProvider(channelId = "room-channel")

        val intent = provider.getIntent(sessionId, roomId, "Room", isDm = false)

        assertThat(intent.action).isEqualTo(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        assertThat(intent.data.toString()).isEqualTo("package:${context.packageName}")
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK).isEqualTo(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun createProvider(channelId: String): DefaultRoomNotificationSettingsIntentProvider {
        return DefaultRoomNotificationSettingsIntentProvider(
            context = context,
            roomNotificationChannelManager = FakeRoomNotificationChannelManager(
                getChannelIdForRoomLambda = { sid, rid, roomDisplayName, isDm, noisy ->
                    assertThat(sid).isEqualTo(sessionId)
                    assertThat(rid).isEqualTo(roomId)
                    assertThat(roomDisplayName).isEqualTo("Room")
                    assertThat(isDm).isFalse()
                    assertThat(noisy).isTrue()
                    channelId
                }
            ),
        )
    }
}
