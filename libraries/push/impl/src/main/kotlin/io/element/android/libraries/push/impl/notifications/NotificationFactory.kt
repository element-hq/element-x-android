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

import android.app.Notification
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.notifications.factories.NotificationFactory
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import javax.inject.Inject

private typealias ProcessedMessageEvents = List<ProcessedEvent<NotifiableMessageEvent>>

// TODO Find a better name, it clashes with io.element.android.libraries.push.impl.notifications.factories.NotificationFactory
class NotificationFactory @Inject constructor(
    private val notificationFactory: NotificationFactory,
    private val roomGroupMessageCreator: RoomGroupMessageCreator,
    private val summaryGroupMessageCreator: SummaryGroupMessageCreator
) {

    fun Map<RoomId, ProcessedMessageEvents>.toNotifications(
        sessionId: SessionId,
        myUserDisplayName: String,
        myUserAvatarUrl: String?
    ): List<RoomNotification> {
        return map { (roomId, events) ->
            when {
                events.hasNoEventsToDisplay() -> RoomNotification.Removed(roomId)
                else -> {
                    val messageEvents = events.onlyKeptEvents().filterNot { it.isRedacted }
                    roomGroupMessageCreator.createRoomMessage(
                        sessionId = sessionId,
                        events = messageEvents,
                        roomId = roomId,
                        userDisplayName = myUserDisplayName,
                        userAvatarUrl = myUserAvatarUrl
                    )
                }
            }
        }
    }

    private fun ProcessedMessageEvents.hasNoEventsToDisplay() = isEmpty() || all {
        it.type == ProcessedEvent.Type.REMOVE || it.event.canNotBeDisplayed()
    }

    private fun NotifiableMessageEvent.canNotBeDisplayed() = isRedacted

    @JvmName("toNotificationsInviteNotifiableEvent")
    fun List<ProcessedEvent<InviteNotifiableEvent>>.toNotifications(): List<OneShotNotification> {
        return map { (processed, event) ->
            when (processed) {
                ProcessedEvent.Type.REMOVE -> OneShotNotification.Removed(key = event.roomId.value)
                ProcessedEvent.Type.KEEP -> OneShotNotification.Append(
                    notificationFactory.createRoomInvitationNotification(event),
                    OneShotNotification.Append.Meta(
                        key = event.roomId.value,
                        summaryLine = event.description,
                        isNoisy = event.noisy,
                        timestamp = event.timestamp
                    )
                )
            }
        }
    }

    @JvmName("toNotificationsSimpleNotifiableEvent")
    fun List<ProcessedEvent<SimpleNotifiableEvent>>.toNotifications(): List<OneShotNotification> {
        return map { (processed, event) ->
            when (processed) {
                ProcessedEvent.Type.REMOVE -> OneShotNotification.Removed(key = event.eventId.value)
                ProcessedEvent.Type.KEEP -> OneShotNotification.Append(
                    notificationFactory.createSimpleEventNotification(event),
                    OneShotNotification.Append.Meta(
                        key = event.eventId.value,
                        summaryLine = event.description,
                        isNoisy = event.noisy,
                        timestamp = event.timestamp
                    )
                )
            }
        }
    }

    fun createSummaryNotification(
        sessionId: SessionId,
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        useCompleteNotificationFormat: Boolean
    ): SummaryNotification {
        val roomMeta = roomNotifications.filterIsInstance<RoomNotification.Message>().map { it.meta }
        val invitationMeta = invitationNotifications.filterIsInstance<OneShotNotification.Append>().map { it.meta }
        val simpleMeta = simpleNotifications.filterIsInstance<OneShotNotification.Append>().map { it.meta }
        return when {
            roomMeta.isEmpty() && invitationMeta.isEmpty() && simpleMeta.isEmpty() -> SummaryNotification.Removed
            else -> SummaryNotification.Update(
                summaryGroupMessageCreator.createSummaryNotification(
                    sessionId = sessionId,
                    roomNotifications = roomMeta,
                    invitationNotifications = invitationMeta,
                    simpleNotifications = simpleMeta,
                    useCompleteNotificationFormat = useCompleteNotificationFormat
                )
            )
        }
    }
}

sealed interface RoomNotification {
    data class Removed(val roomId: RoomId) : RoomNotification
    data class Message(val notification: Notification, val meta: Meta) : RoomNotification {
        data class Meta(
            val roomId: RoomId,
            val summaryLine: CharSequence,
            val messageCount: Int,
            val latestTimestamp: Long,
            val shouldBing: Boolean
        )
    }
}

sealed interface OneShotNotification {
    data class Removed(val key: String) : OneShotNotification
    data class Append(val notification: Notification, val meta: Meta) : OneShotNotification {
        data class Meta(
            val key: String,
            val summaryLine: CharSequence,
            val isNoisy: Boolean,
            val timestamp: Long,
        )
    }
}

sealed interface SummaryNotification {
    object Removed : SummaryNotification
    data class Update(val notification: Notification) : SummaryNotification
}
