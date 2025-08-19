/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.conversations

import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder
import io.element.android.libraries.matrix.ui.media.InitialsAvatarBitmapGenerator
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader
import io.element.android.libraries.push.api.notifications.conversations.NotificationConversationService
import io.element.android.libraries.push.impl.intent.IntentProvider
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultNotificationConversationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intentProvider: IntentProvider,
    private val bitmapLoader: NotificationBitmapLoader,
    private val matrixClientProvider: MatrixClientProvider,
    private val imageLoaderHolder: ImageLoaderHolder,
) : NotificationConversationService {
    override suspend fun onSendMessage(
        sessionId: SessionId,
        roomId: RoomId,
        roomName: String,
        roomIsDirect: Boolean,
        roomAvatarUrl: String?,
    ) {
        val categories = setOfNotNull(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) ShortcutInfo.SHORTCUT_CATEGORY_CONVERSATION else null
        )

        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return
        val imageLoader = imageLoaderHolder.get(client)

        val defaultShortcutIconSize = ShortcutManagerCompat.getIconMaxWidth(context)
        val useDarkTheme = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val icon = bitmapLoader.getRoomBitmap(roomAvatarUrl, imageLoader)?.let(IconCompat::createWithBitmap)
            ?: InitialsAvatarBitmapGenerator(useDarkTheme = useDarkTheme)
                .generateBitmap(defaultShortcutIconSize, AvatarData(id = roomId.value, name = roomName, size = AvatarSize.RoomHeader))
                ?.let(IconCompat::createWithAdaptiveBitmap)

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "$sessionId-$roomId")
            .setShortLabel(roomName)
            .setIcon(icon)
            .setIntent(intentProvider.getViewRoomIntent(sessionId, roomId, threadId = null))
            .setCategories(categories)
            .setLongLived(true)
            .let {
                when (roomIsDirect) {
                    true -> it.addCapabilityBinding("actions.intent.SEND_MESSAGE")
                    false -> it.addCapabilityBinding("actions.intent.SEND_MESSAGE", "message.recipient.@type", listOf("Audience"))
                }
            }
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcutInfo)
    }

    override suspend fun onLeftRoom(sessionId: SessionId, roomId: RoomId) {
        ShortcutManagerCompat.removeDynamicShortcuts(context, listOf("$sessionId-$roomId"))
    }

    override suspend fun onAvailableRoomsChanged(sessionId: SessionId, roomIds: Set<RoomId>) {
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
        }
    }
}
