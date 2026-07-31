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
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.test.FakeLockScreenService
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.ui.media.test.FakeImageLoaderHolder
import io.element.android.libraries.push.impl.notifications.factories.FakeIntentProvider
import io.element.android.libraries.push.impl.notifications.shortcut.createShortcutId
import io.element.android.libraries.push.test.notifications.channels.FakeRoomNotificationChannelManager
import io.element.android.libraries.push.test.notifications.push.FakeNotificationBitmapLoader
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.libraries.sessionstorage.test.observer.FakeSessionObserver
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.annotation.Config
import java.util.Optional
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class DefaultNotificationConversationServiceTest : RobolectricTest() {
    @Test
    fun `onMessageSent adds a shortcut`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val service = createService(context)

        service.onMessageSent(
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
    fun `onMessageReceived adds a shortcut`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val service = createService(context)

        service.onMessageReceived(
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
    fun `ensureRoomShortcut adds a stable long-lived conversation shortcut with room metadata`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        var avatarData: AvatarData? = null
        val bitmapLoader = FakeNotificationBitmapLoader(
            getRoomBitmapResult = { data, _, _ ->
                avatarData = data
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            },
        )
        val service = createService(context, bitmapLoader = bitmapLoader)

        service.ensureRoomShortcut(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            roomName = "Room title",
            roomIsDirect = false,
            roomAvatarUrl = "mxc://avatar",
        )

        val shortcut = ShortcutManagerCompat.getDynamicShortcuts(context).single()
        assertThat(shortcut.id).isEqualTo(createShortcutId(A_SESSION_ID, A_ROOM_ID))
        assertThat(shortcut.shortLabel).isEqualTo("Room title")
        assertThat(shortcut.categories).contains(ShortcutInfo.SHORTCUT_CATEGORY_CONVERSATION)
        assertThat(shortcut.intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(avatarData?.id).isEqualTo(A_ROOM_ID.value)
        assertThat(avatarData?.name).isEqualTo("Room title")
        assertThat(avatarData?.url).isEqualTo("mxc://avatar")
    }

    @Test
    fun `ensureRoomShortcut uses a fallback icon when the room avatar cannot be loaded`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val service = createService(
            context,
            bitmapLoader = FakeNotificationBitmapLoader(
                getRoomBitmapResult = { _, _, _ -> null },
            ),
        )

        service.ensureRoomShortcut(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            roomName = "Room title",
            roomIsDirect = false,
            roomAvatarUrl = null,
        )

        val shortcut = ShortcutManagerCompat.getDynamicShortcuts(context).single()
        assertThat(shortcut.id).isEqualTo(createShortcutId(A_SESSION_ID, A_ROOM_ID))
    }

    @Test
    fun `onMessageReceived does not ensure a notification channel`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        // Default FakeRoomNotificationChannelManager() throws if getChannelIdForRoom is ever
        // called - proving the channel manager is never asked to do anything on receive.
        val service = createService(context, roomNotificationChannelManager = FakeRoomNotificationChannelManager())

        service.onMessageReceived(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            roomName = "Room title",
            roomIsDirect = false,
            roomAvatarUrl = null,
        )

        // No exception means getChannelIdForRoom was never invoked; the shortcut is still pushed.
        assertThat(ShortcutManagerCompat.getDynamicShortcuts(context)).isNotEmpty()
    }

    @Test
    fun `onMessageSent ensures a noisy channel when the room's notification mode is ALL_MESSAGES`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        var noisyPassed: Boolean? = null
        val roomNotificationChannelManager = FakeRoomNotificationChannelManager(
            getChannelIdForRoomLambda = { _, _, _, _, noisy ->
                noisyPassed = noisy
                "a-channel-id"
            },
        )
        val matrixClient = FakeMatrixClient(
            notificationSettingsService = FakeNotificationSettingsService(
                initialRoomMode = RoomNotificationMode.ALL_MESSAGES,
                initialRoomModeIsDefault = false,
            ),
        ).apply {
            getRoomInfoFlowLambda = { flowOf(Optional.of(aRoomInfo(isEncrypted = false))) }
        }
        val matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(matrixClient) })
        val service = createService(
            context,
            roomNotificationChannelManager = roomNotificationChannelManager,
            matrixClientProvider = matrixClientProvider,
        )

        service.onMessageSent(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            roomName = "Room title",
            roomIsDirect = false,
            roomAvatarUrl = null,
        )

        assertThat(noisyPassed).isTrue()
    }

    @Test
    fun `onMessageSent does not promote a channel when the room's notification mode is not ALL_MESSAGES`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        var noisyPassed: Boolean? = null
        val roomNotificationChannelManager = FakeRoomNotificationChannelManager(
            getChannelIdForRoomLambda = { _, _, _, _, noisy ->
                noisyPassed = noisy
                "a-channel-id"
            },
        )
        val matrixClient = FakeMatrixClient(
            notificationSettingsService = FakeNotificationSettingsService(
                initialRoomMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
                initialRoomModeIsDefault = false,
            ),
        ).apply {
            getRoomInfoFlowLambda = { flowOf(Optional.of(aRoomInfo(isEncrypted = false))) }
        }
        val matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(matrixClient) })
        val service = createService(
            context,
            roomNotificationChannelManager = roomNotificationChannelManager,
            matrixClientProvider = matrixClientProvider,
        )

        service.onMessageSent(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            roomName = "Room title",
            roomIsDirect = false,
            roomAvatarUrl = null,
        )

        assertThat(noisyPassed).isFalse()
    }

    @Test
    fun `onMessageSent skips ensuring a channel when the room's encryption state is not yet known`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        // Default FakeMatrixClient().getRoomInfoFlowLambda returns Optional.empty(), i.e. unknown.
        // Default FakeRoomNotificationChannelManager() throws if getChannelIdForRoom is ever called.
        val service = createService(
            context,
            roomNotificationChannelManager = FakeRoomNotificationChannelManager(),
            matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(FakeMatrixClient()) }),
        )

        service.onMessageSent(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            roomName = "Room title",
            roomIsDirect = false,
            roomAvatarUrl = null,
        )

        // No exception means getChannelIdForRoom was never invoked; the shortcut is still pushed.
        assertThat(ShortcutManagerCompat.getDynamicShortcuts(context)).isNotEmpty()
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `on pin code enabled, notification channels are cleared for every session`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val lockScreenService = FakeLockScreenService()
        val clearedSessions = mutableListOf<Any>()
        val roomNotificationChannelManager = FakeRoomNotificationChannelManager(
            clearAllChannelsForSessionLambda = { sessionId -> clearedSessions.add(sessionId) },
        )
        val sessionStore = InMemorySessionStore(
            initialList = listOf(aSessionData(sessionId = A_SESSION_ID.value), aSessionData(sessionId = A_SESSION_ID_2.value)),
        )
        createService(
            context,
            lockScreenService = lockScreenService,
            roomNotificationChannelManager = roomNotificationChannelManager,
            sessionStore = sessionStore,
        )

        lockScreenService.setIsPinSetup(false)
        runCurrent()

        lockScreenService.setIsPinSetup(true)
        runCurrent()

        assertThat(clearedSessions).containsExactly(A_SESSION_ID, A_SESSION_ID_2)
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

    @Test
    fun `onLeftRoom clears the room's notification channel`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        var clearedRoom: Pair<Any, Any>? = null
        val roomNotificationChannelManager = FakeRoomNotificationChannelManager(
            clearRoomChannelLambda = { sessionId, roomId -> clearedRoom = sessionId to roomId },
        )
        val service = createService(context, roomNotificationChannelManager = roomNotificationChannelManager)

        service.onLeftRoom(sessionId = A_SESSION_ID, roomId = A_ROOM_ID)

        assertThat(clearedRoom).isEqualTo(A_SESSION_ID to A_ROOM_ID)
    }

    @Test
    fun `onAvailableRoomsChanged prunes and prunes inactive channels on the channel manager`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        var prunedRooms: Pair<Any, Any>? = null
        var prunedInactiveSession: Any? = null
        val roomNotificationChannelManager = FakeRoomNotificationChannelManager(
            pruneChannelsForSessionLambda = { sessionId, roomIds -> prunedRooms = sessionId to roomIds },
            pruneInactiveChannelsLambda = { sessionId -> prunedInactiveSession = sessionId },
        )
        val service = createService(context, roomNotificationChannelManager = roomNotificationChannelManager)

        service.onAvailableRoomsChanged(sessionId = A_SESSION_ID, roomIds = setOf(A_ROOM_ID))

        assertThat(prunedRooms).isEqualTo(A_SESSION_ID to setOf(A_ROOM_ID))
        assertThat(prunedInactiveSession).isEqualTo(A_SESSION_ID)
    }

    @Test
    fun `on session logged out, the session's notification channels are cleared`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sessionObserver = FakeSessionObserver()
        var clearedSession: Any? = null
        val roomNotificationChannelManager = FakeRoomNotificationChannelManager(
            clearAllChannelsForSessionLambda = { sessionId -> clearedSession = sessionId },
        )
        createService(context, sessionObserver = sessionObserver, roomNotificationChannelManager = roomNotificationChannelManager)

        sessionObserver.onSessionCreated(A_SESSION_ID.value)
        sessionObserver.onSessionDeleted(A_SESSION_ID.value)

        assertThat(clearedSession).isEqualTo(A_SESSION_ID)
    }

    private fun TestScope.createService(
        context: Context = InstrumentationRegistry.getInstrumentation().context,
        sessionObserver: FakeSessionObserver = FakeSessionObserver(),
        lockScreenService: FakeLockScreenService = FakeLockScreenService(),
        matrixClientProvider: FakeMatrixClientProvider = FakeMatrixClientProvider(),
        bitmapLoader: FakeNotificationBitmapLoader = FakeNotificationBitmapLoader(),
        roomNotificationChannelManager: FakeRoomNotificationChannelManager = FakeRoomNotificationChannelManager(
            getChannelIdForRoomLambda = { _, _, _, _, _ -> "a-channel-id" },
            clearRoomChannelLambda = { _, _ -> },
            pruneChannelsForSessionLambda = { _, _ -> },
            clearAllChannelsForSessionLambda = { },
            pruneInactiveChannelsLambda = { },
        ),
        sessionStore: InMemorySessionStore = InMemorySessionStore(),
    ) = DefaultNotificationConversationService(
        context = context,
        intentProvider = FakeIntentProvider(),
        bitmapLoader = bitmapLoader,
        matrixClientProvider = matrixClientProvider,
        imageLoaderHolder = FakeImageLoaderHolder(),
        sessionObserver = sessionObserver,
        lockScreenService = lockScreenService,
        roomNotificationChannelManager = roomNotificationChannelManager,
        sessionStore = sessionStore,
        coroutineScope = backgroundScope,
    )
}
