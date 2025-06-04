/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationManagerCompat
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder
import io.element.android.libraries.push.api.notifications.NotificationCleaner
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.shouldIgnoreEventInRoom
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.NavigationState
import io.element.android.services.appnavstate.api.currentSessionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("DefaultNotificationDrawerManager", LoggerTag.NotificationLoggerTag)

/**
 * This class receives notification events as they arrive from the PushHandler calling [onNotifiableEventReceived] and
 * organise them in order to display them in the notification drawer.
 * Events can be grouped into the same notification, old (already read) events can be removed to do some cleaning.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultNotificationDrawerManager @Inject constructor(
    private val notificationManager: NotificationManagerCompat,
    private val notificationRenderer: NotificationRenderer,
    private val appNavigationStateService: AppNavigationStateService,
    @AppCoroutineScope
    coroutineScope: CoroutineScope,
    private val matrixClientProvider: MatrixClientProvider,
    private val imageLoaderHolder: ImageLoaderHolder,
    private val activeNotificationsProvider: ActiveNotificationsProvider,
) : NotificationCleaner {
    private var appNavigationStateObserver: Job? = null

    // TODO EAx add a setting per user for this
    private var useCompleteNotificationFormat = true

    init {
        // Observe application state
        appNavigationStateObserver = coroutineScope.launch {
            appNavigationStateService.appNavigationState
                .collect { onAppNavigationStateChange(it.navigationState) }
        }
    }

    // For test only
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun destroy() {
        appNavigationStateObserver?.cancel()
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
                onEnteringThread(
                    navigationState.parentRoom.parentSpace.parentSession.sessionId,
                    navigationState.parentRoom.roomId,
                    navigationState.threadId
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
        notificationManager.cancel(null, NotificationIdProvider.getRoomMessagesNotificationId(sessionId))
        clearSummaryNotificationIfNeeded(sessionId)
    }

    /**
     * Clear all notifications related to the session.
     */
    fun clearAllEvents(sessionId: SessionId) {
        activeNotificationsProvider.getNotificationsForSession(sessionId)
            .forEach { notificationManager.cancel(it.tag, it.id) }
    }

    /**
     * Should be called when the application is currently opened and showing timeline for the given [roomId].
     * Used to ignore events related to that room (no need to display notification) and clean any existing notification on this room.
     * Can also be called when a notification for this room is dismissed by the user.
     */
    override fun clearMessagesForRoom(sessionId: SessionId, roomId: RoomId) {
        notificationManager.cancel(roomId.value, NotificationIdProvider.getRoomMessagesNotificationId(sessionId))
        clearSummaryNotificationIfNeeded(sessionId)
    }

    override fun clearMembershipNotificationForSession(sessionId: SessionId) {
        activeNotificationsProvider.getMembershipNotificationForSession(sessionId)
            .forEach { notificationManager.cancel(it.tag, it.id) }
        clearSummaryNotificationIfNeeded(sessionId)
    }

    /**
     * Clear invitation notification for the provided room.
     */
    override fun clearMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId) {
        activeNotificationsProvider.getMembershipNotificationForRoom(sessionId, roomId)
            .forEach { notificationManager.cancel(it.tag, it.id) }
        clearSummaryNotificationIfNeeded(sessionId)
    }

    /**
     * Clear the notifications for a single event.
     */
    override fun clearEvent(sessionId: SessionId, eventId: EventId) {
        val id = NotificationIdProvider.getRoomEventNotificationId(sessionId)
        notificationManager.cancel(eventId.value, id)
        clearSummaryNotificationIfNeeded(sessionId)
    }

    private fun clearSummaryNotificationIfNeeded(sessionId: SessionId) {
        val summaryNotification = activeNotificationsProvider.getSummaryNotification(sessionId)
        if (summaryNotification != null && activeNotificationsProvider.count(sessionId) == 1) {
            notificationManager.cancel(null, summaryNotification.id)
        }
    }

    /**
     * Should be called when the application is currently opened and showing timeline for the given threadId.
     * Used to ignore events related to that thread (no need to display notification) and clean any existing notification on this room.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun onEnteringThread(sessionId: SessionId, roomId: RoomId, threadId: ThreadId) {
        // TODO maybe we'll have to embed more data in the tag to get a threadId
        // Do nothing for now
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
                client.getSafeUserProfile()
            }

            notificationRenderer.render(currentUser, useCompleteNotificationFormat, notifiableEvents, imageLoader)
        }
    }

    private suspend fun MatrixClient.getSafeUserProfile(): MatrixUser {
        return tryOrNull(
            onException = { Timber.tag(loggerTag.value).e(it, "Unable to retrieve info for user ${sessionId.value}") },
            operation = {
                val profile = getUserProfile().getOrNull()
                // displayName cannot be empty else NotificationCompat.MessagingStyle() will crash
                if (profile?.displayName.isNullOrEmpty()) {
                    profile?.copy(displayName = sessionId.value)
                } else {
                    profile
                }
            }
        ) ?: MatrixUser(
            userId = sessionId,
            displayName = sessionId.value,
            avatarUrl = null
        )
    }
}
