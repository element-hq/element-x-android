/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.push.impl.notifications

import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.debug.annotateForDebug
import io.element.android.libraries.push.impl.notifications.factories.NotificationFactory
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.services.toolbox.api.strings.StringProvider
import timber.log.Timber
import javax.inject.Inject

class RoomGroupMessageCreator @Inject constructor(
    private val bitmapLoader: NotificationBitmapLoader,
    private val stringProvider: StringProvider,
    private val notificationFactory: NotificationFactory
) {

    suspend fun createRoomMessage(
        currentUser: MatrixUser,
        events: List<NotifiableMessageEvent>,
        roomId: RoomId,
    ): RoomNotification.Message {
        val lastKnownRoomEvent = events.last()
        val roomName = lastKnownRoomEvent.roomName ?: lastKnownRoomEvent.senderName ?: "Room name (${roomId.value.take(8)}…)"
        val roomIsGroup = !lastKnownRoomEvent.roomIsDirect
        val style = NotificationCompat.MessagingStyle(
            Person.Builder()
                .setName(currentUser.displayName?.annotateForDebug(50))
                .setIcon(bitmapLoader.getUserIcon(currentUser.avatarUrl))
                .setKey(lastKnownRoomEvent.sessionId.value)
                .build()
        ).also {
            it.conversationTitle = roomName.takeIf { roomIsGroup }?.annotateForDebug(51)
            it.isGroupConversation = roomIsGroup
            it.addMessagesFromEvents(events)
        }

        val tickerText = if (roomIsGroup) {
            stringProvider.getString(R.string.notification_ticker_text_group, roomName, events.last().senderName, events.last().description)
        } else {
            stringProvider.getString(R.string.notification_ticker_text_dm, events.last().senderName, events.last().description)
        }

        val largeBitmap = getRoomBitmap(events)

        val lastMessageTimestamp = events.last().timestamp
        val smartReplyErrors = events.filter { it.isSmartReplyError() }
        val messageCount = events.size - smartReplyErrors.size
        val meta = RoomNotification.Message.Meta(
            summaryLine = createRoomMessagesGroupSummaryLine(events, roomName, roomIsDirect = !roomIsGroup),
            messageCount = messageCount,
            latestTimestamp = lastMessageTimestamp,
            roomId = roomId,
            shouldBing = events.any { it.noisy }
        )
        return RoomNotification.Message(
            notificationFactory.createMessagesListNotification(
                style,
                RoomEventGroupInfo(
                    sessionId = currentUser.userId,
                    roomId = roomId,
                    roomDisplayName = roomName,
                    isDirect = !roomIsGroup,
                    hasSmartReplyError = smartReplyErrors.isNotEmpty(),
                    shouldBing = meta.shouldBing,
                    customSound = events.last().soundName,
                    isUpdated = events.last().isUpdated,
                ),
                threadId = lastKnownRoomEvent.threadId,
                largeIcon = largeBitmap,
                lastMessageTimestamp,
                tickerText
            ),
            meta
        )
    }

    private suspend fun NotificationCompat.MessagingStyle.addMessagesFromEvents(events: List<NotifiableMessageEvent>) {
        events.forEach { event ->
            val senderPerson = if (event.outGoingMessage) {
                null
            } else {
                Person.Builder()
                    .setName(event.senderName?.annotateForDebug(70))
                    .setIcon(bitmapLoader.getUserIcon(event.senderAvatarPath))
                    .setKey(event.senderId.value)
                    .build()
            }
            when {
                event.isSmartReplyError() -> addMessage(
                    stringProvider.getString(R.string.notification_inline_reply_failed),
                    event.timestamp,
                    senderPerson
                )
                else -> {
                    val message = NotificationCompat.MessagingStyle.Message(
                        event.body?.annotateForDebug(71),
                        event.timestamp,
                        senderPerson
                    ).also { message ->
                        event.imageUri?.let {
                            message.setData("image/", it)
                        }
                    }
                    addMessage(message)
                }
            }
        }
    }

    private fun createRoomMessagesGroupSummaryLine(events: List<NotifiableMessageEvent>, roomName: String, roomIsDirect: Boolean): CharSequence {
        return try {
            when (events.size) {
                1 -> createFirstMessageSummaryLine(events.first(), roomName, roomIsDirect)
                else -> {
                    stringProvider.getQuantityString(
                        R.plurals.notification_compat_summary_line_for_room,
                        events.size,
                        roomName,
                        events.size
                    )
                }
            }
        } catch (e: Throwable) {
            // String not found or bad format
            Timber.v("%%%%%%%% REFRESH NOTIFICATION DRAWER failed to resolve string")
            roomName
        }
    }

    private fun createFirstMessageSummaryLine(event: NotifiableMessageEvent, roomName: String, roomIsDirect: Boolean): CharSequence {
        return if (roomIsDirect) {
            buildSpannedString {
                inSpans(StyleSpan(Typeface.BOLD)) {
                    append(event.senderName)
                    append(": ")
                }
                append(event.description)
            }
        } else {
            buildSpannedString {
                inSpans(StyleSpan(Typeface.BOLD)) {
                    append(roomName)
                    append(": ")
                    event.senderName
                    append(" ")
                }
                append(event.description)
            }
        }
    }

    private suspend fun getRoomBitmap(events: List<NotifiableMessageEvent>): Bitmap? {
        // Use the last event (most recent?)
        return events.reversed().firstNotNullOfOrNull { it.roomAvatarPath }
            ?.let { bitmapLoader.getRoomBitmap(it) }
    }
}

private fun NotifiableMessageEvent.isSmartReplyError() = outGoingMessage && outGoingMessageFailed
