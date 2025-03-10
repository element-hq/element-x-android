/*
 * Copyright 2021-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.app.Notification
import android.graphics.Typeface
import android.text.style.StyleSpan
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import coil3.ImageLoader
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

interface NotificationDataFactory {
    suspend fun toNotifications(
        messages: List<NotifiableMessageEvent>,
        currentUser: MatrixUser,
        imageLoader: ImageLoader,
    ): List<RoomNotification>

    @JvmName("toNotificationInvites")
    @Suppress("INAPPLICABLE_JVM_NAME")
    fun toNotifications(invites: List<InviteNotifiableEvent>): List<OneShotNotification>
    @JvmName("toNotificationSimpleEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    fun toNotifications(simpleEvents: List<SimpleNotifiableEvent>): List<OneShotNotification>
    @JvmName("toNotificationFallbackEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    fun toNotifications(fallback: List<FallbackNotifiableEvent>): List<OneShotNotification>

    fun createSummaryNotification(
        currentUser: MatrixUser,
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        fallbackNotifications: List<OneShotNotification>,
    ): SummaryNotification
}

@ContributesBinding(AppScope::class)
class DefaultNotificationDataFactory @Inject constructor(
    private val notificationCreator: NotificationCreator,
    private val roomGroupMessageCreator: RoomGroupMessageCreator,
    private val summaryGroupMessageCreator: SummaryGroupMessageCreator,
    private val activeNotificationsProvider: ActiveNotificationsProvider,
    private val stringProvider: StringProvider,
) : NotificationDataFactory {
    override suspend fun toNotifications(
        messages: List<NotifiableMessageEvent>,
        currentUser: MatrixUser,
        imageLoader: ImageLoader,
    ): List<RoomNotification> {
        val messagesToDisplay = messages.filterNot { it.canNotBeDisplayed() }
            .groupBy { it.roomId }
        return messagesToDisplay.map { (roomId, events) ->
            val roomName = events.lastOrNull()?.roomName ?: roomId.value
            val isDm = events.lastOrNull()?.roomIsDm ?: false
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
                summaryLine = createRoomMessagesGroupSummaryLine(events, roomName, isDm),
                messageCount = events.size,
                latestTimestamp = events.maxOf { it.timestamp },
                shouldBing = events.any { it.noisy }
            )
        }
    }

    private fun NotifiableMessageEvent.canNotBeDisplayed() = isRedacted

    private fun getExistingNotificationForMessages(sessionId: SessionId, roomId: RoomId): Notification? {
        return activeNotificationsProvider.getMessageNotificationsForRoom(sessionId, roomId).firstOrNull()?.notification
    }

    @JvmName("toNotificationInvites")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun toNotifications(invites: List<InviteNotifiableEvent>): List<OneShotNotification> {
        return invites.map { event ->
            OneShotNotification(
                key = event.roomId.value,
                notification = notificationCreator.createRoomInvitationNotification(event),
                summaryLine = event.description,
                isNoisy = event.noisy,
                timestamp = event.timestamp
            )
        }
    }

    @JvmName("toNotificationSimpleEvents")
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

    @JvmName("toNotificationFallbackEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
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
                )
            )
        }
    }

    private fun createRoomMessagesGroupSummaryLine(events: List<NotifiableMessageEvent>, roomName: String, roomIsDm: Boolean): CharSequence {
        return when (events.size) {
            1 -> createFirstMessageSummaryLine(events.first(), roomName, roomIsDm)
            else -> {
                stringProvider.getQuantityString(
                    R.plurals.notification_compat_summary_line_for_room,
                    events.size,
                    roomName,
                    events.size
                )
            }
        }
    }

    private fun createFirstMessageSummaryLine(event: NotifiableMessageEvent, roomName: String, roomIsDm: Boolean): CharSequence {
        return if (roomIsDm) {
            buildSpannedString {
                event.senderDisambiguatedDisplayName?.let {
                    inSpans(StyleSpan(Typeface.BOLD)) {
                        append(it)
                        append(": ")
                    }
                }
                append(event.description)
            }
        } else {
            buildSpannedString {
                inSpans(StyleSpan(Typeface.BOLD)) {
                    append(roomName)
                    append(": ")
                    event.senderDisambiguatedDisplayName?.let {
                        append(it)
                        append(" ")
                    }
                }
                append(event.description)
            }
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
) {
    fun isDataEqualTo(other: RoomNotification): Boolean {
        return notification == other.notification &&
            roomId == other.roomId &&
            summaryLine.toString() == other.summaryLine.toString() &&
            messageCount == other.messageCount &&
            latestTimestamp == other.latestTimestamp &&
            shouldBing == other.shouldBing
    }
}

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
