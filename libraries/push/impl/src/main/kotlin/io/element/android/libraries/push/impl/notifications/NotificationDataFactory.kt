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
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import coil.ImageLoader
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import javax.inject.Inject

class NotificationDataFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationCreator: NotificationCreator,
    private val roomGroupMessageCreator: RoomGroupMessageCreator,
    private val summaryGroupMessageCreator: SummaryGroupMessageCreator
) {
    suspend fun List<NotifiableMessageEvent>.toNotifications(
        currentUser: MatrixUser,
        imageLoader: ImageLoader,
    ): List<RoomNotification> {
        val messagesToDisplay = filterNot { it.canNotBeDisplayed() }
            .groupBy { it.roomId }
        return messagesToDisplay.map { (roomId, events) ->
            val notification = roomGroupMessageCreator.createRoomMessage(
                currentUser = currentUser,
                events = events,
                roomId = roomId,
                imageLoader = imageLoader,
                existingNotification = getExistingNotificationForMessages(roomId),
            )
            RoomNotification(
                notification = notification,
                roomId = roomId,
                summaryLine = "${events.size} messages", // events.last().description, // TODO: use a real summary
                messageCount = events.size,
                latestTimestamp = events.maxOf { it.timestamp },
                shouldBing = events.any { it.noisy })
        }
    }

    private fun NotifiableMessageEvent.canNotBeDisplayed() = isRedacted

    private fun getExistingNotificationForMessages(roomId: RoomId): Notification? {
        return NotificationManagerCompat.from(context).activeNotifications.find {it.tag == roomId.value }?.notification
    }

    @JvmName("toNotificationsInviteNotifiableEvent")
    fun List<InviteNotifiableEvent>.toNotifications(): List<OneShotNotification> {
        return map { event ->
            OneShotNotification(
                key = "invite-${event.roomId.value}",
                notification = notificationCreator.createRoomInvitationNotification(event),
                summaryLine = event.description,
                isNoisy = event.noisy,
                timestamp = event.timestamp
            )
        }
    }

    @JvmName("toNotificationsSimpleNotifiableEvent")
    fun List<SimpleNotifiableEvent>.toNotifications(): List<OneShotNotification> {
        return map { event ->
            OneShotNotification(
                key = event.eventId.value,
                notification = notificationCreator.createSimpleEventNotification(event),
                summaryLine = event.description,
                isNoisy = event.noisy,
                timestamp = event.timestamp
            )
        }
    }

    fun List<FallbackNotifiableEvent>.toNotifications(): List<OneShotNotification> {
        return map { event ->
            OneShotNotification(
                key = event.eventId.value,
                notification = notificationCreator.createFallbackNotification(event),
                summaryLine = event.description.orEmpty(),
                isNoisy = false,
                timestamp = event.timestamp
            )
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
        return when {
            roomNotifications.isEmpty() && invitationNotifications.isEmpty() && simpleNotifications.isEmpty() -> SummaryNotification.Removed
            else -> SummaryNotification.Update(
                summaryGroupMessageCreator.createSummaryNotification(
                    currentUser = currentUser,
                    roomNotifications = roomNotifications,
                    invitationNotifications = invitationNotifications,
                    simpleNotifications = simpleNotifications,
                    fallbackNotifications = fallbackNotifications,
                    useCompleteNotificationFormat = useCompleteNotificationFormat
                )
            )
        }
    }
}

data class RoomNotification(
    val notification: Notification,
    val roomId: RoomId,
    val summaryLine: CharSequence,
    val messageCount: Int,
    val latestTimestamp: Long,
    val shouldBing: Boolean,
)

data class OneShotNotification(
    val notification: Notification,
    val key: String,
    val summaryLine: CharSequence,
    val isNoisy: Boolean,
    val timestamp: Long,
)

sealed interface SummaryNotification {
    data object Removed : SummaryNotification
    data class Update(val notification: Notification) : SummaryNotification
}
