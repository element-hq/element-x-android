/*
 * Copyright (c) 2023 New Vector Ltd
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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("NotificationBroadcastReceiver", LoggerTag.NotificationLoggerTag)

/**
 * Receives actions broadcast by notification (on click, on dismiss, inline replies, etc.).
 */
class NotificationBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var defaultNotificationDrawerManager: DefaultNotificationDrawerManager
    @Inject lateinit var actionIds: NotificationActionIds

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return
        context.bindings<NotificationBroadcastReceiverBindings>().inject(this)
        val sessionId = intent.extras?.getString(KEY_SESSION_ID)?.let(::SessionId) ?: return
        val roomId = intent.getStringExtra(KEY_ROOM_ID)?.let(::RoomId)
        val eventId = intent.getStringExtra(KEY_EVENT_ID)?.let(::EventId)
        Timber.tag(loggerTag.value).d("onReceive: ${intent.action} ${intent.data} for: ${roomId?.value}/${eventId?.value}")
        when (intent.action) {
            actionIds.smartReply ->
                handleSmartReply(intent, context)
            actionIds.dismissRoom -> if (roomId != null) {
                defaultNotificationDrawerManager.clearMessagesForRoom(sessionId, roomId, doRender = false)
            }
            actionIds.dismissSummary ->
                defaultNotificationDrawerManager.clearAllMessagesEvents(sessionId, doRender = false)
            actionIds.dismissInvite -> if (roomId != null) {
                defaultNotificationDrawerManager.clearMembershipNotificationForRoom(sessionId, roomId, doRender = false)
            }
            actionIds.dismissEvent -> if (eventId != null) {
                defaultNotificationDrawerManager.clearEvent(sessionId, eventId, doRender = false)
            }
            actionIds.markRoomRead -> if (roomId != null) {
                defaultNotificationDrawerManager.clearMessagesForRoom(sessionId, roomId, doRender = true)
                handleMarkAsRead(sessionId, roomId)
            }
            actionIds.join -> if (roomId != null) {
                defaultNotificationDrawerManager.clearMembershipNotificationForRoom(sessionId, roomId, doRender = true)
                handleJoinRoom(sessionId, roomId)
            }
            actionIds.reject -> if (roomId != null) {
                defaultNotificationDrawerManager.clearMembershipNotificationForRoom(sessionId, roomId, doRender = true)
                handleRejectRoom(sessionId, roomId)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleJoinRoom(sessionId: SessionId, roomId: RoomId) {
        /*
        activeSessionHolder.getSafeActiveSession()?.let { session ->
            val room = session.getRoom(roomId)
            if (room != null) {
                session.coroutineScope.launch {
                    tryOrNull {
                        session.roomService().joinRoom(room.roomId)
                        analyticsTracker.capture(room.roomSummary().toAnalyticsJoinedRoom(JoinedRoom.Trigger.Notification))
                    }
                }
            }
        }
         */
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleRejectRoom(sessionId: SessionId, roomId: RoomId) {
        /*
        activeSessionHolder.getSafeActiveSession()?.let { session ->
            session.coroutineScope.launch {
                tryOrNull { session.roomService().leaveRoom(roomId) }
            }
        }

         */
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleMarkAsRead(sessionId: SessionId, roomId: RoomId) {
        /*
        activeSessionHolder.getActiveSession().let { session ->
            val room = session.getRoom(roomId)
            if (room != null) {
                session.coroutineScope.launch {
                    tryOrNull { room.readService().markAsRead(ReadService.MarkAsReadParams.READ_RECEIPT, mainTimeLineOnly = false) }
                }
            }
        }

         */
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleSmartReply(intent: Intent, context: Context) {
        /*
        val message = getReplyMessage(intent)
        val sessionId = intent.getStringExtra(KEY_SESSION_ID)?.let(::SessionId)
        val roomId = intent.getStringExtra(KEY_ROOM_ID)?.let(::RoomId)
        val threadId = intent.getStringExtra(KEY_THREAD_ID)?.let(::ThreadId)

        if (message.isNullOrBlank() || roomId == null) {
            // ignore this event
            // Can this happen? should we update notification?
            return
        }
        activeSessionHolder.getActiveSession().let { session ->
            session.getRoom(roomId)?.let { room ->
                sendMatrixEvent(message, threadId, session, room, context)
            }
        }
         */
    }

    /*
    private fun sendMatrixEvent(message: String, threadId: String?, session: Session, room: Room, context: Context?) {
        if (threadId != null) {
            room.relationService().replyInThread(
                    rootThreadEventId = threadId,
                    replyInThreadText = message,
            )
        } else {
            room.sendService().sendTextMessage(message)
        }

        // Create a new event to be displayed in the notification drawer, right now

        val notifiableMessageEvent = NotifiableMessageEvent(
                // Generate a Fake event id
                eventId = UUID.randomUUID().toString(),
                editedEventId = null,
                noisy = false,
                timestamp = clock.epochMillis(),
                senderName = session.roomService().getRoomMember(session.myUserId, room.roomId)?.displayName
                        ?: context?.getString(R.string.notification_sender_me),
                senderId = session.myUserId,
                body = message,
                imageUriString = null,
                roomId = room.roomId,
                threadId = threadId,
                roomName = room.roomSummary()?.displayName ?: room.roomId,
                roomIsDirect = room.roomSummary()?.isDirect == true,
                outGoingMessage = true,
                canBeReplaced = false
        )

        notificationDrawerManager.updateEvents { it.onNotifiableEventReceived(notifiableMessageEvent) }

        /*
        // TODO Error cannot be managed the same way than in Riot

        val event = Event(mxMessage, session.credentials.userId, roomId)
        room.storeOutgoingEvent(event)
        room.sendEvent(event, object : MatrixCallback<Void?> {
            override fun onSuccess(info: Void?) {
                Timber.v("Send message : onSuccess ")
            }

            override fun onNetworkError(e: Exception) {
                Timber.e(e, "Send message : onNetworkError")
                onSmartReplyFailed(e.localizedMessage)
            }

            override fun onMatrixError(e: MatrixError) {
                Timber.v("Send message : onMatrixError " + e.message)
                if (e is MXCryptoError) {
                    Toast.makeText(context, e.detailedErrorDescription, Toast.LENGTH_SHORT).show()
                    onSmartReplyFailed(e.detailedErrorDescription)
                } else {
                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    onSmartReplyFailed(e.localizedMessage)
                }
            }

            override fun onUnexpectedError(e: Exception) {
                Timber.e(e, "Send message : onUnexpectedError " + e.message)
                onSmartReplyFailed(e.message)
            }


            fun onSmartReplyFailed(reason: String?) {
                val notifiableMessageEvent = NotifiableMessageEvent(
                        event.eventId,
                        false,
                        clock.epochMillis(),
                        session.myUser?.displayname
                                ?: context?.getString(R.string.notification_sender_me),
                        session.myUserId,
                        message,
                        roomId,
                        room.getRoomDisplayName(context),
                        room.isDirect)
                notifiableMessageEvent.outGoingMessage = true
                notifiableMessageEvent.outGoingMessageFailed = true

                VectorApp.getInstance().notificationDrawerManager.onNotifiableEventReceived(notifiableMessageEvent)
                VectorApp.getInstance().notificationDrawerManager.refreshNotificationDrawer(null)
            }
        })
         */
    }

     */

    /*
    private fun getReplyMessage(intent: Intent?): String? {
        if (intent != null) {
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            if (remoteInput != null) {
                return remoteInput.getCharSequence(KEY_TEXT_REPLY)?.toString()
            }
        }
        return null
    }
     */

    companion object {
        const val KEY_SESSION_ID = "sessionID"
        const val KEY_ROOM_ID = "roomID"
        const val KEY_THREAD_ID = "threadID"
        const val KEY_EVENT_ID = "eventID"
        const val KEY_TEXT_REPLY = "key_text_reply"
    }
}
