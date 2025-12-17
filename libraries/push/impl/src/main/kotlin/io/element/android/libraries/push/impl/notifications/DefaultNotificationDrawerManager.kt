/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder
import io.element.android.libraries.push.api.notifications.NotificationCleaner
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.shouldIgnoreEventInRoom
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.NavigationState
import io.element.android.services.appnavstate.api.currentSessionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * This class receives notification events as they arrive from the PushHandler calling [onNotifiableEventReceived] and
 * organise them in order to display them in the notification drawer.
 * Events can be grouped into the same notification, old (already read) events can be removed to do some cleaning.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultNotificationDrawerManager(
    private val notificationDisplayer: NotificationDisplayer,
    private val notificationRenderer: NotificationRenderer,
    private val appNavigationStateService: AppNavigationStateService,
    @AppCoroutineScope
    coroutineScope: CoroutineScope,
    private val matrixClientProvider: MatrixClientProvider,
    private val imageLoaderHolder: ImageLoaderHolder,
    private val activeNotificationsProvider: ActiveNotificationsProvider,
) : NotificationCleaner {
    // TODO EAx add a setting per user for this
    private var useCompleteNotificationFormat = true

    init {
        // Observe application state
        coroutineScope.launch {
            appNavigationStateService.appNavigationState
                .collect { onAppNavigationStateChange(it.navigationState) }
        }
    }

    private var currentAppNavigationState: NavigationState? = null

    private fun onAppNavigationStateChange(navigationState: NavigationState) {
        when (navigationState) {
            NavigationState.Root -> {
                currentAppNavigationState?.currentSessionId()?.let { sessionId ->
                    // User signed out, clear all notifications related to the session.
                    clearAllEvents(sessionId)
                }
            }
            is NavigationState.Session -> {}
            is NavigationState.Space -> {}
            is NavigationState.Room -> {
                // Cleanup notification for current room
                clearMessagesForRoom(
                    sessionId = navigationState.parentSpace.parentSession.sessionId,
                    roomId = navigationState.roomId,
                )
            }
            is NavigationState.Thread -> {
                clearMessagesForThread(
                    sessionId = navigationState.parentRoom.parentSpace.parentSession.sessionId,
                    roomId = navigationState.parentRoom.roomId,
                    threadId = navigationState.threadId,
                )
            }
        }
        currentAppNavigationState = navigationState
    }

    /**
     * Should be called as soon as a new event is ready to be displayed, filtering out notifications that shouldn't be displayed.
     * Events might be grouped and there might not be one notification per event!
     */
    suspend fun onNotifiableEventReceived(notifiableEvent: NotifiableEvent) {
        if (notifiableEvent.shouldIgnoreEventInRoom(appNavigationStateService.appNavigationState.value)) {
            return
        }
        renderEvents(listOf(notifiableEvent))
    }

    suspend fun onNotifiableEventsReceived(notifiableEvents: List<NotifiableEvent>) {
        val eventsToNotify = notifiableEvents.filter { !it.shouldIgnoreEventInRoom(appNavigationStateService.appNavigationState.value) }
        renderEvents(eventsToNotify)
    }

    /**
     * Clear all known message events for a [sessionId].
     */
    override fun clearAllMessagesEvents(sessionId: SessionId) {
        notificationDisplayer.cancelNotification(null, NotificationIdProvider.getRoomMessagesNotificationId(sessionId))
        clearSummaryNotificationIfNeeded(sessionId)
    }

    /**
     * Clear all notifications related to the session.
     */
    fun clearAllEvents(sessionId: SessionId) {
        activeNotificationsProvider.getNotificationsForSession(sessionId)
            .forEach { notificationDisplayer.cancelNotification(it.tag, it.id) }
    }

    /**
     * Should be called when the application is currently opened and showing timeline for the given [roomId].
     * Used to ignore events related to that room (no need to display notification) and clean any existing notification on this room.
     * Can also be called when a notification for this room is dismissed by the user.
     */
    override fun clearMessagesForRoom(sessionId: SessionId, roomId: RoomId) {
        notificationDisplayer.cancelNotification(roomId.value, NotificationIdProvider.getRoomMessagesNotificationId(sessionId))
        clearSummaryNotificationIfNeeded(sessionId)
    }

    /**
     * Should be called when the application is currently opened and showing timeline for the given threadId.
     * Used to ignore events related to that thread (no need to display notification) and clean any existing notification on this room.
     */
    override fun clearMessagesForThread(sessionId: SessionId, roomId: RoomId, threadId: ThreadId) {
        val tag = NotificationCreator.messageTag(roomId, threadId)
        notificationDisplayer.cancelNotification(tag, NotificationIdProvider.getRoomMessagesNotificationId(sessionId))
        clearSummaryNotificationIfNeeded(sessionId)
    }

    override fun clearMembershipNotificationForSession(sessionId: SessionId) {
        activeNotificationsProvider.getMembershipNotificationForSession(sessionId)
            .forEach { notificationDisplayer.cancelNotification(it.tag, it.id) }
        clearSummaryNotificationIfNeeded(sessionId)
    }

    /**
     * Clear invitation notification for the provided room.
     */
    override fun clearMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId) {
        activeNotificationsProvider.getMembershipNotificationForRoom(sessionId, roomId)
            .forEach { notificationDisplayer.cancelNotification(it.tag, it.id) }
        clearSummaryNotificationIfNeeded(sessionId)
    }

    /**
     * Clear the notifications for a single event.
     */
    override fun clearEvent(sessionId: SessionId, eventId: EventId) {
        val id = NotificationIdProvider.getRoomEventNotificationId(sessionId)
        notificationDisplayer.cancelNotification(eventId.value, id)
        clearSummaryNotificationIfNeeded(sessionId)
    }

    private fun clearSummaryNotificationIfNeeded(sessionId: SessionId) {
        val summaryNotification = activeNotificationsProvider.getSummaryNotification(sessionId)
        if (summaryNotification != null && activeNotificationsProvider.count(sessionId) == 1) {
            notificationDisplayer.cancelNotification(null, summaryNotification.id)
        }
    }

    private suspend fun renderEvents(eventsToRender: List<NotifiableEvent>) {
        // Group by sessionId
        val eventsForSessions = eventsToRender.groupBy {
            it.sessionId
        }

        for ((sessionId, notifiableEvents) in eventsForSessions) {
            val client = matrixClientProvider.getOrRestore(sessionId).getOrThrow()
            val imageLoader = imageLoaderHolder.get(client)
            val userFromCache = client.userProfile.value
            val currentUser = if (userFromCache.avatarUrl != null && userFromCache.displayName.isNullOrEmpty().not()) {
                // We have an avatar and a display name, use it
                userFromCache
            } else {
                client.getUserProfile().getOrNull() ?: MatrixUser(sessionId)
            }
            notificationRenderer.render(currentUser, useCompleteNotificationFormat, notifiableEvents, imageLoader)
        }
    }
}
