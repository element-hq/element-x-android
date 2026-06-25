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
import io.element.android.features.lockscreen.api.LockScreenService
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
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.NavigationState
import io.element.android.services.appnavstate.api.currentRoomId
import io.element.android.services.appnavstate.api.currentSessionId
import io.element.android.services.appnavstate.api.currentThreadId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
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
    private val lockScreenService: LockScreenService,
    sessionObserver: SessionObserver,
) : NotificationCleaner {
    // TODO EAx add a setting per user for this
    private var useCompleteNotificationFormat = true

    private val sessionListener = object : SessionListener {
        override suspend fun onSessionDeleted(userId: String, wasLastSession: Boolean) {
            // User signed out, clear all notifications related to the session.
            clearAllEvents(SessionId(userId))
        }
    }

    init {
        // Observe application state
        coroutineScope.launch {
            appNavigationStateService.appNavigationState
                .collect { onAppNavigationStateChange(it.navigationState) }
        }
        sessionObserver.addListener(sessionListener)
    }

    private fun onAppNavigationStateChange(navigationState: NavigationState) {
        when (navigationState) {
            NavigationState.Root -> {}
            is NavigationState.Session -> {
                // Cleanup the fallback notification
                clearFallbackForSession(navigationState.sessionId)
            }
            is NavigationState.Room -> {
                // Cleanup notification for current room
                clearMessagesForRoom(
                    sessionId = navigationState.parentSession.sessionId,
                    roomId = navigationState.roomId,
                )
            }
            is NavigationState.Thread -> {
                clearMessagesForThread(
                    sessionId = navigationState.parentRoom.parentSession.sessionId,
                    roomId = navigationState.parentRoom.roomId,
                    threadId = navigationState.threadId,
                )
            }
        }
    }

    /**
     * Should be called as soon as a new event is ready to be displayed, filtering out notifications that shouldn't be displayed.
     * Events might be grouped and there might not be one notification per event!
     */
    suspend fun onNotifiableEventReceived(notifiableEvent: NotifiableEvent) {
        onNotifiableEventsReceived(listOf(notifiableEvent))
    }

    suspend fun onNotifiableEventsReceived(notifiableEvents: List<NotifiableEvent>) {
        val eventsToNotify = notifiableEvents.filter { !appNavigationStateService.appNavigationState.value.shouldIgnoreEvent(it) }
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
     * Remove the fallback notification for the session.
     */
    fun clearFallbackForSession(sessionId: SessionId) {
        notificationDisplayer.cancelNotification(
            DefaultNotificationDataFactory.FALLBACK_NOTIFICATION_TAG,
            NotificationIdProvider.getFallbackNotificationId(sessionId),
        )
        clearSummaryNotificationIfNeeded(sessionId)
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

        val isAppLocked = lockScreenService.isPinSetup().first()

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
            if (isAppLocked) {
                // When the app is locked, show a single fallback notification with event count
                // instead of per-room notifications with message content/sender info.
                clearAllMessagesEvents(sessionId)
                val fallbackEvents = notifiableEvents.mapNotNull { it.toFallbackNotifiableEvent() }
                notificationRenderer.render(
                    currentUser = currentUser,
                    useCompleteNotificationFormat = false,
                    eventsToProcess = fallbackEvents,
                    imageLoader = imageLoader,
                )
            } else {
                notificationRenderer.render(
                    currentUser = currentUser,
                    useCompleteNotificationFormat = useCompleteNotificationFormat,
                    eventsToProcess = notifiableEvents,
                    imageLoader = imageLoader,
                )
            }
        }
    }
}

/**
 * Used to check if a notifiableEvent should be ignored based on the current application navigation state.
 */
private fun AppNavigationState.shouldIgnoreEvent(event: NotifiableEvent): Boolean {
    if (!isInForeground) return false
    return navigationState.currentSessionId() == event.sessionId &&
        when (event) {
            is NotifiableRingingCallEvent -> {
                // Never ignore ringing call notifications
                // Note that NotifiableRingingCallEvent are not handled by DefaultNotificationDrawerManager
                false
            }
            is FallbackNotifiableEvent -> {
                // Ignore if the room list is currently displayed
                navigationState is NavigationState.Session
            }
            is InviteNotifiableEvent,
            is SimpleNotifiableEvent -> {
                event.roomId == navigationState.currentRoomId()
            }
            is NotifiableMessageEvent -> {
                event.roomId == navigationState.currentRoomId() &&
                    event.threadId == navigationState.currentThreadId()
            }
        }
}

/**
 * Convert a [NotifiableEvent] into a [FallbackNotifiableEvent], stripping all content and sender info.
 * Used when notification content should be hidden (app locked with PIN).
 */
private fun NotifiableEvent.toFallbackNotifiableEvent(): FallbackNotifiableEvent? {
    val timestamp = when (this) {
        is NotifiableMessageEvent -> timestamp
        is InviteNotifiableEvent -> timestamp
        is SimpleNotifiableEvent -> timestamp
        is FallbackNotifiableEvent -> timestamp
        is NotifiableRingingCallEvent -> return null
    }
    return FallbackNotifiableEvent(
        sessionId = sessionId,
        roomId = roomId,
        eventId = eventId,
        editedEventId = null,
        description = null,
        canBeReplaced = false,
        isRedacted = false,
        isUpdated = false,
        timestamp = timestamp,
        cause = null,
    )
}
