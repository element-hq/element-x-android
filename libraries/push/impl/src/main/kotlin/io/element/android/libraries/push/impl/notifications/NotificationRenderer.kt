/*
 * Copyright 2019 New Vector Ltd
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

import androidx.annotation.WorkerThread
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import timber.log.Timber
import javax.inject.Inject

class NotificationRenderer @Inject constructor(
    private val notificationIdProvider: NotificationIdProvider,
    private val notificationDisplayer: NotificationDisplayer,
    private val notificationFactory: NotificationFactory,
) {

    @WorkerThread
    fun render(
        sessionId: SessionId,
        myUserDisplayName: String,
        myUserAvatarUrl: String?,
        useCompleteNotificationFormat: Boolean,
        eventsToProcess: List<ProcessedEvent<NotifiableEvent>>
    ) {
        val (roomEvents, simpleEvents, invitationEvents) = eventsToProcess.groupByType()
        with(notificationFactory) {
            val roomNotifications = roomEvents.toNotifications(sessionId, myUserDisplayName, myUserAvatarUrl)
            val invitationNotifications = invitationEvents.toNotifications()
            val simpleNotifications = simpleEvents.toNotifications()
            val summaryNotification = createSummaryNotification(
                sessionId = sessionId,
                roomNotifications = roomNotifications,
                invitationNotifications = invitationNotifications,
                simpleNotifications = simpleNotifications,
                useCompleteNotificationFormat = useCompleteNotificationFormat
            )

            // Remove summary first to avoid briefly displaying it after dismissing the last notification
            if (summaryNotification == SummaryNotification.Removed) {
                Timber.d("Removing summary notification")
                notificationDisplayer.cancelNotificationMessage(null, notificationIdProvider.getSummaryNotificationId(sessionId))
            }

            roomNotifications.forEach { wrapper ->
                when (wrapper) {
                    is RoomNotification.Removed -> {
                        Timber.d("Removing room messages notification ${wrapper.roomId}")
                        notificationDisplayer.cancelNotificationMessage(wrapper.roomId.value, notificationIdProvider.getRoomMessagesNotificationId(sessionId))
                    }
                    is RoomNotification.Message -> if (useCompleteNotificationFormat) {
                        Timber.d("Updating room messages notification ${wrapper.meta.roomId}")
                        notificationDisplayer.showNotificationMessage(
                            wrapper.meta.roomId.value,
                            notificationIdProvider.getRoomMessagesNotificationId(sessionId),
                            wrapper.notification
                        )
                    }
                }
            }

            invitationNotifications.forEach { wrapper ->
                when (wrapper) {
                    is OneShotNotification.Removed -> {
                        Timber.d("Removing invitation notification ${wrapper.key}")
                        notificationDisplayer.cancelNotificationMessage(wrapper.key, notificationIdProvider.getRoomInvitationNotificationId(sessionId))
                    }
                    is OneShotNotification.Append -> if (useCompleteNotificationFormat) {
                        Timber.d("Updating invitation notification ${wrapper.meta.key}")
                        notificationDisplayer.showNotificationMessage(
                            wrapper.meta.key,
                            notificationIdProvider.getRoomInvitationNotificationId(sessionId),
                            wrapper.notification
                        )
                    }
                }
            }

            simpleNotifications.forEach { wrapper ->
                when (wrapper) {
                    is OneShotNotification.Removed -> {
                        Timber.d("Removing simple notification ${wrapper.key}")
                        notificationDisplayer.cancelNotificationMessage(wrapper.key, notificationIdProvider.getRoomEventNotificationId(sessionId))
                    }
                    is OneShotNotification.Append -> if (useCompleteNotificationFormat) {
                        Timber.d("Updating simple notification ${wrapper.meta.key}")
                        notificationDisplayer.showNotificationMessage(
                            wrapper.meta.key,
                            notificationIdProvider.getRoomEventNotificationId(sessionId),
                            wrapper.notification
                        )
                    }
                }
            }

            // Update summary last to avoid briefly displaying it before other notifications
            if (summaryNotification is SummaryNotification.Update) {
                Timber.d("Updating summary notification")
                notificationDisplayer.showNotificationMessage(
                    null,
                    notificationIdProvider.getSummaryNotificationId(sessionId),
                    summaryNotification.notification
                )
            }
        }
    }

    fun cancelAllNotifications() {
        notificationDisplayer.cancelAllNotifications()
    }
}

private fun List<ProcessedEvent<NotifiableEvent>>.groupByType(): GroupedNotificationEvents {
    val roomIdToEventMap: MutableMap<RoomId, MutableList<ProcessedEvent<NotifiableMessageEvent>>> = LinkedHashMap()
    val simpleEvents: MutableList<ProcessedEvent<SimpleNotifiableEvent>> = ArrayList()
    val invitationEvents: MutableList<ProcessedEvent<InviteNotifiableEvent>> = ArrayList()
    forEach {
        when (val event = it.event) {
            is InviteNotifiableEvent -> invitationEvents.add(it.castedToEventType())
            is NotifiableMessageEvent -> {
                val roomEvents = roomIdToEventMap.getOrPut(event.roomId) { ArrayList() }
                roomEvents.add(it.castedToEventType())
            }
            is SimpleNotifiableEvent -> simpleEvents.add(it.castedToEventType())
        }
    }
    return GroupedNotificationEvents(roomIdToEventMap, simpleEvents, invitationEvents)
}

@Suppress("UNCHECKED_CAST")
private fun <T : NotifiableEvent> ProcessedEvent<NotifiableEvent>.castedToEventType(): ProcessedEvent<T> = this as ProcessedEvent<T>

data class GroupedNotificationEvents(
    val roomEvents: Map<RoomId, List<ProcessedEvent<NotifiableMessageEvent>>>,
    val simpleEvents: List<ProcessedEvent<SimpleNotifiableEvent>>,
    val invitationEvents: List<ProcessedEvent<InviteNotifiableEvent>>
)
