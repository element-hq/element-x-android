/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.push.api.notifications.conversations.NotificationConversationService
import io.element.android.libraries.push.impl.notifications.DefaultNotificationDrawerManager
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface OnNotifiableEventReceived {
    fun onNotifiableEventsReceived(notifiableEvents: List<NotifiableEvent>)
}

@ContributesBinding(AppScope::class)
class DefaultOnNotifiableEventReceived(
    private val defaultNotificationDrawerManager: DefaultNotificationDrawerManager,
    private val notificationConversationService: NotificationConversationService,
    @AppCoroutineScope
    private val coroutineScope: CoroutineScope,
) : OnNotifiableEventReceived {
    override fun onNotifiableEventsReceived(notifiableEvents: List<NotifiableEvent>) {
        coroutineScope.launch {
            notificationConversationService.pushShortcutsForIncomingMessages(notifiableEvents)
            defaultNotificationDrawerManager.onNotifiableEventsReceived(notifiableEvents.filter { it !is NotifiableRingingCallEvent })
        }
    }
}

/**
 * Otherwise, [NotificationConversationService.onSendMessage] (despite the name, it just
 * creates/refreshes a room's conversation shortcut) is only called from the message composer, so
 * a room you've only ever received messages in never gets a shortcut - and without one, its
 * per-room notification channel has no icon/avatar to show in system Settings until you reply.
 */
internal suspend fun NotificationConversationService.pushShortcutsForIncomingMessages(notifiableEvents: List<NotifiableEvent>) {
    notifiableEvents
        .asSequence()
        .filterIsInstance<NotifiableMessageEvent>()
        .filter { !it.outGoingMessage && it.threadId == null }
        .distinctBy { it.sessionId to it.roomId }
        .forEach { event ->
            onSendMessage(
                sessionId = event.sessionId,
                roomId = event.roomId,
                roomName = event.roomName ?: event.roomId.value,
                roomIsDirect = event.roomIsDm,
                roomAvatarUrl = event.roomAvatarPath,
            )
        }
}
