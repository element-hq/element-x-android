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
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader
import io.element.android.libraries.push.api.notifications.conversations.NotificationConversationService
import io.element.android.libraries.push.impl.intent.IntentProvider
import io.element.android.libraries.push.impl.notifications.shortcut.createShortcutId
import io.element.android.libraries.push.impl.notifications.shortcut.filterBySession
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultNotificationConversationService(
    @ApplicationContext private val context: Context,
    private val intentProvider: IntentProvider,
    private val bitmapLoader: NotificationBitmapLoader,
    private val matrixClientProvider: MatrixClientProvider,
    private val imageLoaderHolder: ImageLoaderHolder,
    private val lockScreenService: LockScreenService,
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
                    clearShortcuts()
                }
            }
            .launchIn(coroutineScope)
    }

    override suspend fun onSendMessage(
        sessionId: SessionId,
        roomId: RoomId,
        roomName: String,
        roomIsDirect: Boolean,
        roomAvatarUrl: String?,
    ) {
        if (lockScreenService.isPinSetup().first()) {
            // We don't create shortcuts when a pin code is set for privacy reasons
            return
        }

        val categories = setOfNotNull(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) ShortcutInfo.SHORTCUT_CATEGORY_CONVERSATION else null
        )

        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return
        val imageLoader = imageLoaderHolder.get(client)

        val defaultShortcutIconSize = ShortcutManagerCompat.getIconMaxWidth(context)
        val icon = bitmapLoader.getRoomBitmap(
            avatarData = AvatarData(
                id = roomId.value,
                name = roomName,
                url = roomAvatarUrl,
                size = AvatarSize.RoomDetailsHeader,
            ),
            imageLoader = imageLoader,
            targetSize = defaultShortcutIconSize.toLong()
        )?.let(IconCompat::createWithBitmap)

        val shortcutInfo = ShortcutInfoCompat.Builder(context, createShortcutId(sessionId, roomId))
            .setShortLabel(roomName)
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
    }

    private fun clearShortcuts() {
        runCatchingExceptions {
            ShortcutManagerCompat.removeAllDynamicShortcuts(context)
        }.onFailure {
            Timber.e(it, "Failed to clear all shortcuts")
        }
    }

    private fun onSessionLogOut(sessionId: SessionId) {
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
    }
}
