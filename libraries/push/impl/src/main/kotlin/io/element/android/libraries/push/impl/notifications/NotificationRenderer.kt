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

import coil.ImageLoader
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("NotificationRenderer", LoggerTag.NotificationLoggerTag)

class NotificationRenderer @Inject constructor(
    private val notificationDisplayer: NotificationDisplayer,
    private val notificationDataFactory: NotificationDataFactory,
) {
    suspend fun render(
        currentUser: MatrixUser,
        useCompleteNotificationFormat: Boolean,
        eventsToProcess: List<NotifiableEvent>,
        imageLoader: ImageLoader,
    ) {
        val groupedEvents = eventsToProcess.groupByType()
        with(notificationDataFactory) {
            val roomNotifications = toNotifications(groupedEvents.roomEvents, currentUser, imageLoader)
            val invitationNotifications = toNotifications(groupedEvents.invitationEvents)
            val simpleNotifications = toNotifications(groupedEvents.simpleEvents)
            val fallbackNotifications = toNotifications(groupedEvents.fallbackEvents)
            val summaryNotification = createSummaryNotification(
                currentUser = currentUser,
                roomNotifications = roomNotifications,
                invitationNotifications = invitationNotifications,
                simpleNotifications = simpleNotifications,
                fallbackNotifications = fallbackNotifications,
            )

            // Remove summary first to avoid briefly displaying it after dismissing the last notification
            if (summaryNotification == SummaryNotification.Removed) {
                Timber.tag(loggerTag.value).d("Removing summary notification")
                notificationDisplayer.cancelNotificationMessage(
                    tag = null,
                    id = NotificationIdProvider.getSummaryNotificationId(currentUser.userId)
                )
            }

            roomNotifications.forEach { notificationData ->
                notificationDisplayer.showNotificationMessage(
                    tag = notificationData.roomId.value,
                    id = NotificationIdProvider.getRoomMessagesNotificationId(currentUser.userId),
                    notification = notificationData.notification
                )
            }

            invitationNotifications.forEach { notificationData ->
                if (useCompleteNotificationFormat) {
                    Timber.tag(loggerTag.value).d("Updating invitation notification ${notificationData.key}")
                    notificationDisplayer.showNotificationMessage(
                        tag = notificationData.key,
                        id = NotificationIdProvider.getRoomInvitationNotificationId(currentUser.userId),
                        notification = notificationData.notification
                    )
                }
            }

            simpleNotifications.forEach { notificationData ->
                if (useCompleteNotificationFormat) {
                    Timber.tag(loggerTag.value).d("Updating simple notification ${notificationData.key}")
                    notificationDisplayer.showNotificationMessage(
                        tag = notificationData.key,
                        id = NotificationIdProvider.getRoomEventNotificationId(currentUser.userId),
                        notification = notificationData.notification
                    )
                }
            }

            // Show only the first fallback notification
            if (fallbackNotifications.isNotEmpty()) {
                Timber.tag(loggerTag.value).d("Showing fallback notification")
                notificationDisplayer.showNotificationMessage(
                    tag = "FALLBACK",
                    id = NotificationIdProvider.getFallbackNotificationId(currentUser.userId),
                    notification = fallbackNotifications.first().notification
                )
            }

            // Update summary last to avoid briefly displaying it before other notifications
            if (summaryNotification is SummaryNotification.Update) {
                Timber.tag(loggerTag.value).d("Updating summary notification")
                notificationDisplayer.showNotificationMessage(
                    tag = null,
                    id = NotificationIdProvider.getSummaryNotificationId(currentUser.userId),
                    notification = summaryNotification.notification
                )
            }
        }
    }
}

private fun List<NotifiableEvent>.groupByType(): GroupedNotificationEvents {
    val roomEvents: MutableList<NotifiableMessageEvent> = mutableListOf()
    val simpleEvents: MutableList<SimpleNotifiableEvent> = mutableListOf()
    val invitationEvents: MutableList<InviteNotifiableEvent> = mutableListOf()
    val fallbackEvents: MutableList<FallbackNotifiableEvent> = mutableListOf()
    forEach { event ->
        when (event) {
            is InviteNotifiableEvent -> invitationEvents.add(event.castedToEventType())
            is NotifiableMessageEvent -> roomEvents.add(event.castedToEventType())
            is SimpleNotifiableEvent -> simpleEvents.add(event.castedToEventType())
            is FallbackNotifiableEvent -> fallbackEvents.add(event.castedToEventType())
            // Nothing should be done for ringing call events as they're not handled here
            is NotifiableRingingCallEvent -> {}
        }
    }
    return GroupedNotificationEvents(roomEvents, simpleEvents, invitationEvents, fallbackEvents)
}

@Suppress("UNCHECKED_CAST")
private fun <T : NotifiableEvent> NotifiableEvent.castedToEventType(): T = this as T

data class GroupedNotificationEvents(
    val roomEvents: List<NotifiableMessageEvent>,
    val simpleEvents: List<SimpleNotifiableEvent>,
    val invitationEvents: List<InviteNotifiableEvent>,
    val fallbackEvents: List<FallbackNotifiableEvent>,
)
