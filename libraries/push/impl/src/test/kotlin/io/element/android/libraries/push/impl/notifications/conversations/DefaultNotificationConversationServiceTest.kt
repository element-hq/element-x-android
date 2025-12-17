/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
import io.element.android.features.lockscreen.test.FakeLockScreenService
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.ui.media.test.FakeImageLoaderHolder
import io.element.android.libraries.push.impl.notifications.factories.FakeIntentProvider
import io.element.android.libraries.push.impl.notifications.shortcut.createShortcutId
import io.element.android.libraries.push.test.notifications.push.FakeNotificationBitmapLoader
import io.element.android.libraries.sessionstorage.test.observer.FakeSessionObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
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
        )

        val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
        assertThat(shortcuts).isNotEmpty()
    }

    @Test
    fun `onLeftRoom removes a shortcut`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val service = createService(context)

        val shortcutId = createShortcutId(A_SESSION_ID, A_ROOM_ID)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `on pin code enabled, all shortcuts are cleared`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val lockScreenService = FakeLockScreenService()
        createService(context, lockScreenService = lockScreenService)

        // Make sure the pin is disabled
        lockScreenService.setIsPinSetup(false)
        // Give the test some time to save the pin setup value
        runCurrent()

        // We add a couple of shortcuts from different sessions
        val shortcutInfoA = ShortcutInfoCompat.Builder(context, "$A_SESSION_ID-$A_ROOM_ID")
            .setShortLabel("Room title")
            .setIntent(Intent(Intent.ACTION_VIEW))
            .build()
        val shortcutInfoB = ShortcutInfoCompat.Builder(context, "$A_SESSION_ID_2-$A_ROOM_ID_2")
            .setShortLabel("Room title")
            .setIntent(Intent(Intent.ACTION_VIEW))
            .build()
        ShortcutManagerCompat.setDynamicShortcuts(context, listOf(shortcutInfoA, shortcutInfoB))
        assertThat(ShortcutManagerCompat.getDynamicShortcuts(context)).hasSize(2)

        // Enable the pin code
        lockScreenService.setIsPinSetup(true)
        // Give the test some time to save the new pin setup value
        runCurrent()

        // Then we check there are no shortcuts left from any session
        val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
        assertThat(shortcuts).isEmpty()
    }

    @Test
    fun `on session logged out, all shortcuts for the session are cleared`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sessionObserver = FakeSessionObserver()
        createService(context, sessionObserver = sessionObserver)

        // Set the initial session state
        sessionObserver.onSessionCreated(A_SESSION_ID.value)
        sessionObserver.onSessionCreated(A_SESSION_ID_2.value)

        // We add a couple of shortcuts from different sessions
        val shortcutInfoA = ShortcutInfoCompat.Builder(context, "$A_SESSION_ID-$A_ROOM_ID")
            .setShortLabel("Room title")
            .setIntent(Intent(Intent.ACTION_VIEW))
            .build()
        val shortcutInfoB = ShortcutInfoCompat.Builder(context, "$A_SESSION_ID_2-$A_ROOM_ID_2")
            .setShortLabel("Room title")
            .setIntent(Intent(Intent.ACTION_VIEW))
            .build()
        ShortcutManagerCompat.setDynamicShortcuts(context, listOf(shortcutInfoA, shortcutInfoB))
        assertThat(ShortcutManagerCompat.getDynamicShortcuts(context)).hasSize(2)

        // A session is logged out
        sessionObserver.onSessionDeleted(A_SESSION_ID.value)

        // Then we check the shortcuts for the logged out session are removed, but the rest remain
        val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
        assertThat(shortcuts).hasSize(1)
        assertThat(shortcuts.first().id).startsWith(A_SESSION_ID_2.value)
    }

    private fun TestScope.createService(
        context: Context = InstrumentationRegistry.getInstrumentation().context,
        sessionObserver: FakeSessionObserver = FakeSessionObserver(),
        lockScreenService: FakeLockScreenService = FakeLockScreenService(),
    ) = DefaultNotificationConversationService(
        context = context,
        intentProvider = FakeIntentProvider(),
        bitmapLoader = FakeNotificationBitmapLoader(),
        matrixClientProvider = FakeMatrixClientProvider(),
        imageLoaderHolder = FakeImageLoaderHolder(),
        sessionObserver = sessionObserver,
        lockScreenService = lockScreenService,
        coroutineScope = backgroundScope,
    )
}
