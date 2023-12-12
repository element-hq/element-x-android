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
import coil.ImageLoader
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import javax.inject.Inject

private typealias ProcessedMessageEvents = List<ProcessedEvent<NotifiableMessageEvent>>

class NotificationFactory @Inject constructor(
    private val notificationCreator: NotificationCreator,
    private val roomGroupMessageCreator: RoomGroupMessageCreator,
    private val summaryGroupMessageCreator: SummaryGroupMessageCreator
) {

    suspend fun Map<RoomId, ProcessedMessageEvents>.toNotifications(
        currentUser: MatrixUser,
        imageLoader: ImageLoader,
    ): List<RoomNotification> {
        return map { (roomId, events) ->
            when {
                events.hasNoEventsToDisplay() -> RoomNotification.Removed(roomId)
                else -> {
                    val messageEvents = events.onlyKeptEvents().filterNot { it.isRedacted }
                    roomGroupMessageCreator.createRoomMessage(
                        currentUser = currentUser,
                        events = messageEvents,
                        roomId = roomId,
                        imageLoader = imageLoader,
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
                    notificationCreator.createRoomInvitationNotification(event),
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
                    notificationCreator.createSimpleEventNotification(event),
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

    fun List<ProcessedEvent<FallbackNotifiableEvent>>.toNotifications(): List<OneShotNotification> {
        return map { (processed, event) ->
            when (processed) {
                ProcessedEvent.Type.REMOVE -> OneShotNotification.Removed(key = event.eventId.value)
                ProcessedEvent.Type.KEEP -> OneShotNotification.Append(
                    notificationCreator.createFallbackNotification(event),
                    OneShotNotification.Append.Meta(
                        key = event.eventId.value,
                        summaryLine = event.description.orEmpty(),
                        isNoisy = false,
                        timestamp = event.timestamp
                    )
                )
            }
        }
    }

    fun createSummaryNotification(
        currentUser: MatrixUser,
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        fallbackNotifications: List<OneShotNotification>,
        useCompleteNotificationFormat: Boolean
    ): SummaryNotification {
        val roomMeta = roomNotifications.filterIsInstance<RoomNotification.Message>().map { it.meta }
        val invitationMeta = invitationNotifications.filterIsInstance<OneShotNotification.Append>().map { it.meta }
        val simpleMeta = simpleNotifications.filterIsInstance<OneShotNotification.Append>().map { it.meta }
        val fallbackMeta = fallbackNotifications.filterIsInstance<OneShotNotification.Append>().map { it.meta }
        return when {
            roomMeta.isEmpty() && invitationMeta.isEmpty() && simpleMeta.isEmpty() -> SummaryNotification.Removed
            else -> SummaryNotification.Update(
                summaryGroupMessageCreator.createSummaryNotification(
                    currentUser = currentUser,
                    roomNotifications = roomMeta,
                    invitationNotifications = invitationMeta,
                    simpleNotifications = simpleMeta,
                    fallbackNotifications = fallbackMeta,
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
    data object Removed : SummaryNotification
    data class Update(val notification: Notification) : SummaryNotification
}
