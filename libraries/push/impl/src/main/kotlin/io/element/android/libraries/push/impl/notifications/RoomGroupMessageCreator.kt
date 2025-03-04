/*
 * Copyright 2021-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.app.Notification
import android.graphics.Bitmap
import coil3.ImageLoader
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.factories.isSmartReplyError
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

interface RoomGroupMessageCreator {
    suspend fun createRoomMessage(
        currentUser: MatrixUser,
        events: List<NotifiableMessageEvent>,
        roomId: RoomId,
        imageLoader: ImageLoader,
        existingNotification: Notification?,
    ): Notification
}

@ContributesBinding(AppScope::class)
class DefaultRoomGroupMessageCreator @Inject constructor(
    private val bitmapLoader: NotificationBitmapLoader,
    private val stringProvider: StringProvider,
    private val notificationCreator: NotificationCreator,
) : RoomGroupMessageCreator {
    override suspend fun createRoomMessage(
        currentUser: MatrixUser,
        events: List<NotifiableMessageEvent>,
        roomId: RoomId,
        imageLoader: ImageLoader,
        existingNotification: Notification?,
    ): Notification {
        val lastKnownRoomEvent = events.last()
        val roomName = lastKnownRoomEvent.roomName ?: lastKnownRoomEvent.senderDisambiguatedDisplayName ?: "Room name (${roomId.value.take(8)}…)"
        val roomIsGroup = !lastKnownRoomEvent.roomIsDm

        val tickerText = if (roomIsGroup) {
            stringProvider.getString(R.string.notification_ticker_text_group, roomName, events.last().senderDisambiguatedDisplayName, events.last().description)
        } else {
            stringProvider.getString(R.string.notification_ticker_text_dm, events.last().senderDisambiguatedDisplayName, events.last().description)
        }

        val largeBitmap = getRoomBitmap(events, imageLoader)

        val lastMessageTimestamp = events.last().timestamp
        val smartReplyErrors = events.filter { it.isSmartReplyError() }
        val roomIsDm = !roomIsGroup
        return notificationCreator.createMessagesListNotification(
                RoomEventGroupInfo(
                    sessionId = currentUser.userId,
                    roomId = roomId,
                    roomDisplayName = roomName,
                    isDm = roomIsDm,
                    hasSmartReplyError = smartReplyErrors.isNotEmpty(),
                    shouldBing = events.any { it.noisy },
                    customSound = events.last().soundName,
                    isUpdated = events.last().isUpdated,
                ),
                threadId = lastKnownRoomEvent.threadId,
                largeIcon = largeBitmap,
                lastMessageTimestamp = lastMessageTimestamp,
                tickerText = tickerText,
                currentUser = currentUser,
                existingNotification = existingNotification,
                imageLoader = imageLoader,
                events = events,
        )
    }

    private suspend fun getRoomBitmap(
        events: List<NotifiableMessageEvent>,
        imageLoader: ImageLoader,
    ): Bitmap? {
        // Use the last event (most recent?)
        return events.reversed().firstNotNullOfOrNull { it.roomAvatarPath }
            ?.let { bitmapLoader.getRoomBitmap(it, imageLoader) }
    }
}
