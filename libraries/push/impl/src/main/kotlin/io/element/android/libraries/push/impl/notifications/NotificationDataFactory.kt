/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2021-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.app.Notification
import coil3.ImageLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.push.impl.notifications.factories.NotificationAccountParams
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent

interface NotificationDataFactory {
    suspend fun toNotifications(
        messages: List<NotifiableMessageEvent>,
        imageLoader: ImageLoader,
        notificationAccountParams: NotificationAccountParams,
    ): List<RoomNotification>

    @JvmName("toNotificationInvites")
    @Suppress("INAPPLICABLE_JVM_NAME")
    fun toNotifications(
        invites: List<InviteNotifiableEvent>,
        notificationAccountParams: NotificationAccountParams,
    ): List<OneShotNotification>

    @JvmName("toNotificationSimpleEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    fun toNotifications(
        simpleEvents: List<SimpleNotifiableEvent>,
        notificationAccountParams: NotificationAccountParams,
    ): List<OneShotNotification>

    @JvmName("toNotificationFallbackEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    fun toNotification(
        fallback: List<FallbackNotifiableEvent>,
        notificationAccountParams: NotificationAccountParams,
    ): OneShotNotification?

    fun createSummaryNotification(
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        notificationAccountParams: NotificationAccountParams,
    ): SummaryNotification
}

@ContributesBinding(AppScope::class)
class DefaultNotificationDataFactory(
    private val notificationCreator: NotificationCreator,
    private val roomGroupMessageCreator: RoomGroupMessageCreator,
    private val summaryGroupMessageCreator: SummaryGroupMessageCreator,
    private val activeNotificationsProvider: ActiveNotificationsProvider,
) : NotificationDataFactory {
    override suspend fun toNotifications(
        messages: List<NotifiableMessageEvent>,
        imageLoader: ImageLoader,
        notificationAccountParams: NotificationAccountParams,
    ): List<RoomNotification> {
        val messagesToDisplay = messages.filterNot { it.canNotBeDisplayed() }
            .groupBy { it.roomId }
        return messagesToDisplay.flatMap { (roomId, events) ->
            val eventsByThreadId = events.groupBy { it.threadId }
            eventsByThreadId.map { (threadId, events) ->
                val notification = roomGroupMessageCreator.createRoomMessage(
                    events = events,
                    roomId = roomId,
                    threadId = threadId,
                    imageLoader = imageLoader,
                    existingNotification = getExistingNotificationForMessages(notificationAccountParams.user.userId, roomId, threadId),
                    notificationAccountParams = notificationAccountParams,
                )
                RoomNotification(
                    notification = notification,
                    roomId = roomId,
                    threadId = threadId,
                    messageCount = events.size,
                    latestTimestamp = events.maxOf { it.timestamp },
                    shouldBing = events.any { it.noisy }
                )
            }
        }
    }

    private fun NotifiableMessageEvent.canNotBeDisplayed() = isRedacted

    private fun getExistingNotificationForMessages(sessionId: SessionId, roomId: RoomId, threadId: ThreadId?): Notification? {
        return activeNotificationsProvider.getMessageNotificationsForRoom(sessionId, roomId, threadId).firstOrNull()?.notification
    }

    @JvmName("toNotificationInvites")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun toNotifications(
        invites: List<InviteNotifiableEvent>,
        notificationAccountParams: NotificationAccountParams,
    ): List<OneShotNotification> {
        return invites.map { event ->
            OneShotNotification(
                tag = event.roomId.value,
                notification = notificationCreator.createRoomInvitationNotification(notificationAccountParams, event),
                isNoisy = event.noisy,
                timestamp = event.timestamp
            )
        }
    }

    @JvmName("toNotificationSimpleEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun toNotifications(
        simpleEvents: List<SimpleNotifiableEvent>,
        notificationAccountParams: NotificationAccountParams,
    ): List<OneShotNotification> {
        return simpleEvents.map { event ->
            OneShotNotification(
                tag = event.eventId.value,
                notification = notificationCreator.createSimpleEventNotification(notificationAccountParams, event),
                isNoisy = event.noisy,
                timestamp = event.timestamp
            )
        }
    }

    @JvmName("toNotificationFallbackEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun toNotification(
        fallback: List<FallbackNotifiableEvent>,
        notificationAccountParams: NotificationAccountParams,
    ): OneShotNotification? {
        if (fallback.isEmpty()) return null
        val existingNotification = activeNotificationsProvider
            .getFallbackNotification(notificationAccountParams.user.userId)
            ?.notification
        val notification = notificationCreator.createFallbackNotification(
            existingNotification = existingNotification,
            notificationAccountParams = notificationAccountParams,
            fallbackNotifiableEvents = fallback,
        )
        return OneShotNotification(
            tag = FALLBACK_NOTIFICATION_TAG,
            notification = notification,
            isNoisy = false,
            timestamp = fallback.first().timestamp
        )
    }

    override fun createSummaryNotification(
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        notificationAccountParams: NotificationAccountParams,
    ): SummaryNotification {
        return when {
            roomNotifications.isEmpty() && invitationNotifications.isEmpty() && simpleNotifications.isEmpty() -> SummaryNotification.Removed
            else -> SummaryNotification.Update(
                summaryGroupMessageCreator.createSummaryNotification(
                    roomNotifications = roomNotifications,
                    invitationNotifications = invitationNotifications,
                    simpleNotifications = simpleNotifications,
                    notificationAccountParams = notificationAccountParams,
                )
            )
        }
    }

    companion object {
        const val FALLBACK_NOTIFICATION_TAG = "FALLBACK"
    }
}

data class RoomNotification(
    val notification: Notification,
    val roomId: RoomId,
    val threadId: ThreadId?,
    val messageCount: Int,
    val latestTimestamp: Long,
    val shouldBing: Boolean,
) {
    fun isDataEqualTo(other: RoomNotification): Boolean {
        return notification == other.notification &&
            roomId == other.roomId &&
            threadId == other.threadId &&
            messageCount == other.messageCount &&
            latestTimestamp == other.latestTimestamp &&
            shouldBing == other.shouldBing
    }
}

data class OneShotNotification(
    val notification: Notification,
    val tag: String,
    val isNoisy: Boolean,
    val timestamp: Long,
)

sealed interface SummaryNotification {
    data object Removed : SummaryNotification
    data class Update(val notification: Notification) : SummaryNotification
}
