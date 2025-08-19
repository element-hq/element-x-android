/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.conversations

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.push.impl.notifications.factories.FakeIntentProvider
import io.element.android.libraries.push.test.notifications.FakeImageLoaderHolder
import io.element.android.libraries.push.test.notifications.push.FakeNotificationBitmapLoader
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class DefaultNotificationConversationServiceTest {
    @Test
    fun `onSendMessage adds a shortcut`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val service = createService(context)

        service.onSendMessage(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            roomName = "Room title",
            roomIsDirect = false,
            roomAvatarUrl = null,
            threadId = null
        )

        val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
        assertThat(shortcuts).isNotEmpty()
    }

    @Test
    fun `onLeftRoom removes a shortcut`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val service = createService(context)

        val shortcutId = "$A_SESSION_ID-$A_ROOM_ID"
        val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel("Room title")
            .setIntent(Intent(Intent.ACTION_VIEW))
            .build()

        // First we add the shortcut
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcutInfo)

        assertThat(ShortcutManagerCompat.getDynamicShortcuts(context).firstOrNull()?.id).isEqualTo(shortcutId)

        service.onLeftRoom(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
        )

        // Then we check it's removed
        val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
        assertThat(shortcuts).isEmpty()
    }

    @Test
    fun `onAvailableRoomsChanged keeps only the available rooms as shortcuts`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val service = createService(context)

        // We add a couple of shortcuts
        val shortcutInfoA = ShortcutInfoCompat.Builder(context, "$A_SESSION_ID-$A_ROOM_ID")
            .setShortLabel("Room title")
            .setIntent(Intent(Intent.ACTION_VIEW))
            .build()
        val shortcutInfoB = ShortcutInfoCompat.Builder(context, "$A_SESSION_ID-$A_ROOM_ID_2")
            .setShortLabel("Room title")
            .setIntent(Intent(Intent.ACTION_VIEW))
            .build()
        ShortcutManagerCompat.setDynamicShortcuts(context, listOf(shortcutInfoA, shortcutInfoB))

        assertThat(ShortcutManagerCompat.getDynamicShortcuts(context)).hasSize(2)

        service.onAvailableRoomsChanged(
            sessionId = A_SESSION_ID,
            roomIds = setOf(A_ROOM_ID),
        )

        // Then we check only the shortcuts for the matching rooms remain
        val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
        assertThat(shortcuts).hasSize(1)
        assertThat(shortcuts.first().id).isEqualTo("$A_SESSION_ID-$A_ROOM_ID")
    }

    private fun createService(
        context: Context = InstrumentationRegistry.getInstrumentation().context,
    ) = DefaultNotificationConversationService(
        context = context,
        intentProvider = FakeIntentProvider(),
        bitmapLoader = FakeNotificationBitmapLoader(),
        matrixClientProvider = FakeMatrixClientProvider(),
        imageLoaderHolder = FakeImageLoaderHolder(),
    )
}
