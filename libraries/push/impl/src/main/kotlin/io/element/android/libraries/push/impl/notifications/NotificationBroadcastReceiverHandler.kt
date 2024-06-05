/*
 * Copyright (c) 2024 New Vector Ltd
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

import android.content.Intent
import io.element.android.features.preferences.api.store.SessionPreferencesStoreFactory
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.asEventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.push.api.notifications.NotificationDrawerManager
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.push.OnNotifiableEventReceived
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

private val loggerTag = LoggerTag("NotificationBroadcastReceiverHandler", LoggerTag.NotificationLoggerTag)

class NotificationBroadcastReceiverHandler @Inject constructor(
    private val appCoroutineScope: CoroutineScope,
    private val matrixClientProvider: MatrixClientProvider,
    private val sessionPreferencesStore: SessionPreferencesStoreFactory,
    private val notificationDrawerManager: NotificationDrawerManager,
    private val actionIds: NotificationActionIds,
    private val systemClock: SystemClock,
    private val onNotifiableEventReceived: OnNotifiableEventReceived,
    private val stringProvider: StringProvider,
    private val replyMessageExtractor: ReplyMessageExtractor,
) {
    fun onReceive(intent: Intent) {
        val sessionId = intent.getStringExtra(NotificationBroadcastReceiver.KEY_SESSION_ID)?.let(::SessionId) ?: return
        val roomId = intent.getStringExtra(NotificationBroadcastReceiver.KEY_ROOM_ID)?.let(::RoomId)
        val threadId = intent.getStringExtra(NotificationBroadcastReceiver.KEY_THREAD_ID)?.let(::ThreadId)
        val eventId = intent.getStringExtra(NotificationBroadcastReceiver.KEY_EVENT_ID)?.let(::EventId)

        Timber.tag(loggerTag.value).d("onReceive: ${intent.action} ${intent.data} for: ${roomId?.value}/${eventId?.value}")
        when (intent.action) {
            actionIds.smartReply -> if (roomId != null) {
                handleSmartReply(sessionId, roomId, threadId, intent)
            }
            actionIds.dismissRoom -> if (roomId != null) {
                notificationDrawerManager.clearMessagesForRoom(sessionId, roomId)
            }
            actionIds.dismissSummary ->
                notificationDrawerManager.clearAllMessagesEvents(sessionId)
            actionIds.dismissInvite -> if (roomId != null) {
                notificationDrawerManager.clearMembershipNotificationForRoom(sessionId, roomId)
            }
            actionIds.dismissEvent -> if (eventId != null) {
                notificationDrawerManager.clearEvent(sessionId, eventId)
            }
            actionIds.markRoomRead -> if (roomId != null) {
                notificationDrawerManager.clearMessagesForRoom(sessionId, roomId)
                handleMarkAsRead(sessionId, roomId)
            }
            actionIds.join -> if (roomId != null) {
                notificationDrawerManager.clearMembershipNotificationForRoom(sessionId, roomId)
                handleJoinRoom(sessionId, roomId)
            }
            actionIds.reject -> if (roomId != null) {
                notificationDrawerManager.clearMembershipNotificationForRoom(sessionId, roomId)
                handleRejectRoom(sessionId, roomId)
            }
        }
    }

    private fun handleJoinRoom(sessionId: SessionId, roomId: RoomId) = appCoroutineScope.launch {
        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return@launch
        client.joinRoom(roomId)
    }

    private fun handleRejectRoom(sessionId: SessionId, roomId: RoomId) = appCoroutineScope.launch {
        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return@launch
        client.getRoom(roomId)?.leave()
    }

    private fun handleMarkAsRead(sessionId: SessionId, roomId: RoomId) = appCoroutineScope.launch {
        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return@launch
        val isSendPublicReadReceiptsEnabled = sessionPreferencesStore.get(sessionId, this).isSendPublicReadReceiptsEnabled().first()
        val receiptType = if (isSendPublicReadReceiptsEnabled) {
            ReceiptType.READ
        } else {
            ReceiptType.READ_PRIVATE
        }
        client.getRoom(roomId)?.markAsRead(receiptType = receiptType)
    }

    private fun handleSmartReply(
        sessionId: SessionId,
        roomId: RoomId,
        threadId: ThreadId?,
        intent: Intent,
    ) = appCoroutineScope.launch {
        val message = replyMessageExtractor.getReplyMessage(intent)
        if (message.isNullOrBlank()) {
            // ignore this event
            // Can this happen? should we update notification?
            return@launch
        }
        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return@launch
        client.getRoom(roomId)?.let { room ->
            sendMatrixEvent(
                sessionId = sessionId,
                roomId = roomId,
                threadId = threadId,
                room = room,
                message = message,
            )
        }
    }

    private suspend fun sendMatrixEvent(
        sessionId: SessionId,
        roomId: RoomId,
        threadId: ThreadId?,
        room: MatrixRoom,
        message: String,
    ) {
        // Create a new event to be displayed in the notification drawer, right now
        val notifiableMessageEvent = NotifiableMessageEvent(
            sessionId = sessionId,
            roomId = roomId,
            // Generate a Fake event id
            eventId = EventId("\$" + UUID.randomUUID().toString()),
            editedEventId = null,
            canBeReplaced = false,
            senderId = sessionId,
            noisy = false,
            timestamp = systemClock.epochMillis(),
            senderDisambiguatedDisplayName = room.getUpdatedMember(sessionId).getOrNull()
                ?.disambiguatedDisplayName
                ?: stringProvider.getString(R.string.notification_sender_me),
            body = message,
            imageUriString = null,
            threadId = threadId,
            roomName = room.displayName,
            roomIsDirect = room.isDirect,
            outGoingMessage = true,
        )
        onNotifiableEventReceived.onNotifiableEventReceived(notifiableMessageEvent)

        if (threadId != null) {
            room.liveTimeline.replyMessage(
                eventId = threadId.asEventId(),
                body = message,
                htmlBody = null,
                mentions = emptyList(),
                fromNotification = true,
            )
        } else {
            room.liveTimeline.sendMessage(
                body = message,
                htmlBody = null,
                mentions = emptyList()
            )
        }.onFailure {
            Timber.e(it, "Failed to send smart reply message")
            onNotifiableEventReceived.onNotifiableEventReceived(
                notifiableMessageEvent.copy(
                    outGoingMessageFailed = true
                )
            )
        }
    }
}
