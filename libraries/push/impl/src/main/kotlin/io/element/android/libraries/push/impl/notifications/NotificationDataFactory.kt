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
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import javax.inject.Inject

interface NotificationDataFactory {
    suspend fun toNotifications(
        messages: List<NotifiableMessageEvent>,
        currentUser: MatrixUser,
        imageLoader: ImageLoader,
    ): List<RoomNotification>

    @JvmName("toNofificationInvites")
    @Suppress("INAPPLICABLE_JVM_NAME")
    fun toNotifications(invites: List<InviteNotifiableEvent>): List<OneShotNotification>
    @JvmName("toNofificationSimpleEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    fun toNotifications(simpleEvents: List<SimpleNotifiableEvent>): List<OneShotNotification>
    fun toNotifications(fallback: List<FallbackNotifiableEvent>): List<OneShotNotification>

    fun createSummaryNotification(
        currentUser: MatrixUser,
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        fallbackNotifications: List<OneShotNotification>,
        useCompleteNotificationFormat: Boolean
    ): SummaryNotification
}

class DefaultNotificationDataFactory @Inject constructor(
    private val notificationCreator: NotificationCreator,
    private val roomGroupMessageCreator: RoomGroupMessageCreator,
    private val summaryGroupMessageCreator: SummaryGroupMessageCreator,
    private val activeNotificationsProvider: ActiveNotificationsProvider,
) : NotificationDataFactory {
    override suspend fun toNotifications(
        messages: List<NotifiableMessageEvent>,
        currentUser: MatrixUser,
        imageLoader: ImageLoader,
    ): List<RoomNotification> {
        val messagesToDisplay = messages.filterNot { it.canNotBeDisplayed() }
            .groupBy { it.roomId }
        return messagesToDisplay.map { (roomId, events) ->
            val notification = roomGroupMessageCreator.createRoomMessage(
                currentUser = currentUser,
                events = events,
                roomId = roomId,
                imageLoader = imageLoader,
                existingNotification = getExistingNotificationForMessages(currentUser.userId, roomId),
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

    private fun getExistingNotificationForMessages(sessionId: SessionId, roomId: RoomId): Notification? {
        return activeNotificationsProvider.getNotificationsForRoom(sessionId, roomId).firstOrNull()?.notification
    }

    @JvmName("toNofificationInvites")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun toNotifications(invites: List<InviteNotifiableEvent>): List<OneShotNotification> {
        return invites.map { event ->
            OneShotNotification(
                key = "invite-${event.roomId.value}",
                notification = notificationCreator.createRoomInvitationNotification(event),
                summaryLine = event.description,
                isNoisy = event.noisy,
                timestamp = event.timestamp
            )
        }
    }

    @JvmName("toNofificationSimpleEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun toNotifications(simpleEvents: List<SimpleNotifiableEvent>): List<OneShotNotification> {
        return simpleEvents.map { event ->
            OneShotNotification(
                key = event.eventId.value,
                notification = notificationCreator.createSimpleEventNotification(event),
                summaryLine = event.description,
                isNoisy = event.noisy,
                timestamp = event.timestamp
            )
        }
    }

    override fun toNotifications(fallback: List<FallbackNotifiableEvent>): List<OneShotNotification> {
        return fallback.map { event ->
            OneShotNotification(
                key = event.eventId.value,
                notification = notificationCreator.createFallbackNotification(event),
                summaryLine = event.description.orEmpty(),
                isNoisy = false,
                timestamp = event.timestamp
            )
        }
    }

    override fun createSummaryNotification(
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
