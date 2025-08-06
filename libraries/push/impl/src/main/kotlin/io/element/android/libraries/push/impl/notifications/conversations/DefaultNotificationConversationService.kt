/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.conversations

import android.content.Context
import android.content.pm.ShortcutInfo
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
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.ui.media.DefaultImageLoaderHolder
import io.element.android.libraries.matrix.ui.media.InitialsAvatarBitmapGenerator
import io.element.android.libraries.push.api.notifications.conversations.NotificationConversationService
import io.element.android.libraries.push.impl.intent.IntentProvider
import io.element.android.libraries.push.impl.notifications.DefaultNotificationBitmapLoader
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultNotificationConversationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intentProvider: IntentProvider,
    private val bitmapLoader: DefaultNotificationBitmapLoader,
    private val matrixClientProvider: MatrixClientProvider,
    private val imageLoaderHolder: DefaultImageLoaderHolder,
) : NotificationConversationService {
    override suspend fun onSendMessage(
        sessionId: SessionId,
        roomId: RoomId,
        roomName: String,
        roomIsDirect: Boolean,
        roomAvatarUrl: String?,
        threadId: ThreadId?,
    ) {
        val categories = setOfNotNull(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) ShortcutInfo.SHORTCUT_CATEGORY_CONVERSATION else null
        )

        val client = matrixClientProvider.getOrRestore(sessionId).getOrThrow()
        val imageLoader = imageLoaderHolder.get(client)
        val icon = bitmapLoader.getRoomBitmap(roomAvatarUrl, imageLoader)?.let(IconCompat::createWithBitmap)
            ?: InitialsAvatarBitmapGenerator()
                .generateBitmap(512, AvatarData(id = roomId.value, name = roomName, size = AvatarSize.RoomHeader))?.let(
                IconCompat::createWithAdaptiveBitmap
            )

        val shortcutInfo = ShortcutInfoCompat.Builder(context, roomId.value)
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
}
