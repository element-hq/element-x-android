/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import androidx.compose.ui.graphics.toArgb
import coil3.ImageLoader
import dev.zacsweers.metro.Inject
import io.element.android.appconfig.NotificationConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import kotlinx.coroutines.flow.first
import timber.log.Timber

private val loggerTag = LoggerTag("NotificationRenderer", LoggerTag.NotificationLoggerTag)

@Inject
class NotificationRenderer(
    private val notificationDisplayer: NotificationDisplayer,
    private val notificationDataFactory: NotificationDataFactory,
    private val enterpriseService: EnterpriseService,
) {
    suspend fun render(
        currentUser: MatrixUser,
        useCompleteNotificationFormat: Boolean,
        eventsToProcess: List<NotifiableEvent>,
        imageLoader: ImageLoader,
    ) {
        val color = enterpriseService.brandColorsFlow(currentUser.userId).first()?.toArgb()
            ?: NotificationConfig.NOTIFICATION_ACCENT_COLOR
        val groupedEvents = eventsToProcess.groupByType()
        val roomNotifications = notificationDataFactory.toNotifications(groupedEvents.roomEvents, currentUser, imageLoader, color)
        val invitationNotifications = notificationDataFactory.toNotifications(groupedEvents.invitationEvents, color)
        val simpleNotifications = notificationDataFactory.toNotifications(groupedEvents.simpleEvents, color)
        val fallbackNotifications = notificationDataFactory.toNotifications(groupedEvents.fallbackEvents, color)
        val summaryNotification = notificationDataFactory.createSummaryNotification(
            currentUser = currentUser,
            roomNotifications = roomNotifications,
            invitationNotifications = invitationNotifications,
            simpleNotifications = simpleNotifications,
            fallbackNotifications = fallbackNotifications,
            color = color,
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
            val tag = if (notificationData.threadId != null) {
                "${notificationData.roomId}|${notificationData.threadId}"
            } else {
                notificationData.roomId.value
            }
            notificationDisplayer.showNotificationMessage(
                tag = tag,
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
