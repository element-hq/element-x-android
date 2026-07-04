/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.conversations

import android.content.Context
import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.libraries.core.coroutine.withPreviousValue
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader
import io.element.android.libraries.push.api.notifications.RoomNotificationChannelManager
import io.element.android.libraries.push.api.notifications.conversations.NotificationConversationService
import io.element.android.libraries.push.impl.intent.IntentProvider
import io.element.android.libraries.push.impl.notifications.shortcut.createShortcutId
import io.element.android.libraries.push.impl.notifications.shortcut.filterBySession
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import kotlin.jvm.optionals.getOrNull

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultNotificationConversationService(
    @ApplicationContext private val context: Context,
    private val intentProvider: IntentProvider,
    private val bitmapLoader: NotificationBitmapLoader,
    private val matrixClientProvider: MatrixClientProvider,
    private val imageLoaderHolder: ImageLoaderHolder,
    private val lockScreenService: LockScreenService,
    private val roomNotificationChannelManager: RoomNotificationChannelManager,
    private val sessionStore: SessionStore,
    sessionObserver: SessionObserver,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
) : NotificationConversationService {
    private val isRequestPinShortcutSupported = ShortcutManagerCompat.isRequestPinShortcutSupported(context)

    init {
        sessionObserver.addListener(object : SessionListener {
            override suspend fun onSessionDeleted(userId: String, wasLastSession: Boolean) {
                onSessionLogOut(SessionId(userId))
            }
        })

        lockScreenService.isPinSetup()
            .withPreviousValue()
            .onEach { (hadPinCode, hasPinCode) ->
                if (hadPinCode == false && hasPinCode) {
                    // Shortcuts and per-room channels both surface a room's display name/icon in
                    // system UI (launcher, Settings) regardless of in-app lock state, so both must
                    // be wiped the moment the user opts into hiding that - not just shortcuts.
                    clearShortcuts()
                    clearAllRoomChannelsForAllSessions()
                }
            }
            .launchIn(coroutineScope)
    }

    override suspend fun onMessageSent(
        sessionId: SessionId,
        roomId: RoomId,
        roomName: String?,
        roomIsDirect: Boolean,
        roomAvatarUrl: String?,
    ) {
        val pushed = pushConversationShortcut(sessionId, roomId, roomName, roomIsDirect, roomAvatarUrl) ?: return
        ensureRoomNotificationChannel(pushed.client, sessionId, roomId, pushed.roomDisplayName, roomIsDirect)
    }

    override suspend fun onMessageReceived(
        sessionId: SessionId,
        roomId: RoomId,
        roomName: String?,
        roomIsDirect: Boolean,
        roomAvatarUrl: String?,
    ) {
        // Deliberately does not call ensureRoomNotificationChannel: DefaultNotificationCreator
        // already ensures the channel for a genuinely noisy incoming notification, using that
        // event's actual noisiness rather than the room's static mode. Doing it again here would
        // duplicate that work (extra SDK round-trips on the path to showing the notification) and
        // could disagree with it.
        pushConversationShortcut(sessionId, roomId, roomName, roomIsDirect, roomAvatarUrl)
    }

    private class PushedShortcut(val client: MatrixClient, val roomDisplayName: String)

    private suspend fun pushConversationShortcut(
        sessionId: SessionId,
        roomId: RoomId,
        roomName: String?,
        roomIsDirect: Boolean,
        roomAvatarUrl: String?,
    ): PushedShortcut? {
        if (lockScreenService.isPinSetup().first()) {
            // We don't create shortcuts when a pin code is set for privacy reasons
            return null
        }

        val categories = setOfNotNull(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) ShortcutInfo.SHORTCUT_CATEGORY_CONVERSATION else null
        )

        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return null
        val imageLoader = imageLoaderHolder.get(client)

        val defaultShortcutIconSize = ShortcutManagerCompat.getIconMaxWidth(context)
        val name = roomName?.takeIf { it.isNotBlank() } ?: roomId.value
        val icon = bitmapLoader.getRoomBitmap(
            avatarData = AvatarData(
                id = roomId.value,
                name = name,
                url = roomAvatarUrl,
                size = AvatarSize.RoomDetailsHeader,
            ),
            imageLoader = imageLoader,
            targetSize = defaultShortcutIconSize.toLong()
        )?.let(IconCompat::createWithBitmap)

        val shortcutInfo = ShortcutInfoCompat.Builder(context, createShortcutId(sessionId, roomId))
            .setShortLabel(name)
            .setIcon(icon)
            .setIntent(intentProvider.getViewRoomIntent(sessionId, roomId, threadId = null, eventId = null))
            .setCategories(categories)
            .setLongLived(true)
            .let {
                when (roomIsDirect) {
                    true -> it.addCapabilityBinding("actions.intent.SEND_MESSAGE")
                    false -> it.addCapabilityBinding("actions.intent.SEND_MESSAGE", "message.recipient.@type", listOf("Audience"))
                }
            }
            .build()

        runCatchingExceptions { ShortcutManagerCompat.pushDynamicShortcut(context, shortcutInfo) }
            .onFailure {
                Timber.e(it, "Failed to create shortcut for room $roomId in session $sessionId")
            }

        return PushedShortcut(client, name)
    }

    /**
     * A room's per-room channel is otherwise only created from a genuinely noisy incoming
     * notification (see [RoomNotificationChannelManager]), so a room you've only ever sent
     * messages in - e.g. a self-chat, which never generates a notification for its own sender -
     * would never get one. Mirror that same "genuinely noisy" gate here using the room's actual
     * notification mode, rather than assuming every send should promote the room.
     */
    private suspend fun ensureRoomNotificationChannel(
        client: MatrixClient,
        sessionId: SessionId,
        roomId: RoomId,
        roomDisplayName: String,
        isDm: Boolean,
    ) {
        runCatchingExceptions {
            val isEncrypted = client.getRoomInfoFlow(roomId).first().getOrNull()?.isEncrypted
            if (isEncrypted == null) {
                // The room's (or specifically its encryption) state hasn't synced yet. Guessing
                // unencrypted could resolve against the wrong default push rule bucket, so skip
                // rather than promote on a guess - a later send or receive in this room will
                // retry once it's known.
                Timber.d("Skipping notification channel for room $roomId in session $sessionId: encryption state not yet known")
                return@runCatchingExceptions
            }
            val mode = client.notificationSettingsService.getRoomNotificationSettings(roomId, isEncrypted, isDm).getOrNull()?.mode
            roomNotificationChannelManager.getChannelIdForRoom(
                sessionId = sessionId,
                roomId = roomId,
                roomDisplayName = roomDisplayName,
                isDm = isDm,
                noisy = mode == RoomNotificationMode.ALL_MESSAGES,
            )
        }.onFailure {
            Timber.e(it, "Failed to ensure notification channel for room $roomId in session $sessionId")
        }
    }

    private suspend fun clearAllRoomChannelsForAllSessions() {
        runCatchingExceptions { sessionStore.getAllSessions() }
            .onFailure { Timber.e(it, "Failed to list sessions to clear notification channels after enabling PIN lock") }
            .getOrElse { emptyList() }
            .forEach { sessionData ->
                runCatchingExceptions {
                    roomNotificationChannelManager.clearAllChannelsForSession(SessionId(sessionData.userId))
                }.onFailure {
                    Timber.e(it, "Failed to clear notification channels for session ${sessionData.userId} after enabling PIN lock")
                }
            }
    }

    override suspend fun onLeftRoom(sessionId: SessionId, roomId: RoomId) {
        val shortcutsToRemove = listOf(createShortcutId(sessionId, roomId))
        runCatchingExceptions {
            ShortcutManagerCompat.removeDynamicShortcuts(context, shortcutsToRemove)
            if (isRequestPinShortcutSupported) {
                ShortcutManagerCompat.disableShortcuts(
                    context,
                    shortcutsToRemove,
                    context.getString(CommonStrings.common_android_shortcuts_remove_reason_left_room)
                )
            }
        }.onFailure {
            Timber.e(it, "Failed to remove shortcut for room $roomId in session $sessionId")
        }
        runCatchingExceptions {
            roomNotificationChannelManager.clearRoomChannel(sessionId, roomId)
        }.onFailure {
            Timber.e(it, "Failed to clear notification channel for room $roomId in session $sessionId")
        }
    }

    override suspend fun onAvailableRoomsChanged(sessionId: SessionId, roomIds: Set<RoomId>) {
        runCatchingExceptions {
            val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)

            val shortcutsToRemove = mutableListOf<String>()
            shortcuts.filter { it.id.startsWith(sessionId.value) }
                .forEach { shortcut ->
                    val roomId = RoomId(shortcut.id.removePrefix("$sessionId-"))
                    if (!roomIds.contains(roomId)) {
                        shortcutsToRemove.add(shortcut.id)
                    }
                }

            if (shortcutsToRemove.isNotEmpty()) {
                ShortcutManagerCompat.removeDynamicShortcuts(context, shortcutsToRemove)
                if (isRequestPinShortcutSupported) {
                    ShortcutManagerCompat.disableShortcuts(
                        context,
                        shortcutsToRemove,
                        context.getString(CommonStrings.common_android_shortcuts_remove_reason_left_room)
                    )
                }
            }
        }.onFailure {
            Timber.e(it, "Failed to remove shortcuts for session $sessionId")
        }
        runCatchingExceptions {
            roomNotificationChannelManager.pruneChannelsForSession(sessionId, roomIds)
        }.onFailure {
            Timber.e(it, "Failed to prune notification channels for session $sessionId")
        }
        runCatchingExceptions {
            roomNotificationChannelManager.pruneInactiveChannels(sessionId)
        }.onFailure {
            Timber.e(it, "Failed to prune inactive notification channels for session $sessionId")
        }
    }

    private fun clearShortcuts() {
        runCatchingExceptions {
            ShortcutManagerCompat.removeAllDynamicShortcuts(context)
        }.onFailure {
            Timber.e(it, "Failed to clear all shortcuts")
        }
    }

    private suspend fun onSessionLogOut(sessionId: SessionId) {
        runCatchingExceptions {
            val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
            val shortcutIdsToRemove = shortcuts.filterBySession(sessionId).map { it.id }
            ShortcutManagerCompat.removeDynamicShortcuts(context, shortcutIdsToRemove)

            if (isRequestPinShortcutSupported) {
                ShortcutManagerCompat.disableShortcuts(
                    context,
                    shortcutIdsToRemove,
                    context.getString(CommonStrings.common_android_shortcuts_remove_reason_session_logged_out)
                )
            }
        }.onFailure {
            Timber.e(it, "Failed to remove shortcuts for session $sessionId after logout")
        }
        runCatchingExceptions {
            roomNotificationChannelManager.clearAllChannelsForSession(sessionId)
        }.onFailure {
            Timber.e(it, "Failed to clear notification channels for session $sessionId after logout")
        }
    }
}
