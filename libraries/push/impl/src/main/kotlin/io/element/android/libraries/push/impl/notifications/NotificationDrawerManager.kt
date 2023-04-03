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

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.WorkerThread
import io.element.android.libraries.androidutils.throttler.FirstThrottler
import io.element.android.libraries.core.cache.CircularCache
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.push.api.store.PushDataStore
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.shouldIgnoreMessageEventInRoom
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.AppNavigationStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * The NotificationDrawerManager receives notification events as they arrived (from event stream or fcm) and
 * organise them in order to display them in the notification drawer.
 * Events can be grouped into the same notification, old (already read) events can be removed to do some cleaning.
 */
@SingleIn(AppScope::class)
class NotificationDrawerManager @Inject constructor(
    @ApplicationContext context: Context,
    private val pushDataStore: PushDataStore,
    private val notifiableEventProcessor: NotifiableEventProcessor,
    private val notificationRenderer: NotificationRenderer,
    private val notificationEventPersistence: NotificationEventPersistence,
    private val filteredEventDetector: FilteredEventDetector,
    private val appNavigationStateService: AppNavigationStateService,
    private val coroutineScope: CoroutineScope,
    private val buildMeta: BuildMeta,
) {

    private val handlerThread: HandlerThread = HandlerThread("NotificationDrawerManager", Thread.MIN_PRIORITY)
    private var backgroundHandler: Handler

    /**
     * Lazily initializes the NotificationState as we rely on having a current session in order to fetch the persisted queue of events.
     */
    private val notificationState by lazy { createInitialNotificationState() }
    private val avatarSize = context.resources.getDimensionPixelSize(R.dimen.profile_avatar_size)
    private var currentAppNavigationState: AppNavigationState? = null
    private val firstThrottler = FirstThrottler(200)

    private var useCompleteNotificationFormat = pushDataStore.useCompleteNotificationFormat()

    init {
        handlerThread.start()
        backgroundHandler = Handler(handlerThread.looper)
        // Observe application state
        coroutineScope.launch {
            appNavigationStateService.appNavigationStateFlow
                .collect { onAppNavigationStateChange(it) }
        }
    }

    private fun onAppNavigationStateChange(appNavigationState: AppNavigationState) {
        currentAppNavigationState = appNavigationState
        when (appNavigationState) {
            AppNavigationState.Root -> {}
            is AppNavigationState.Session -> {}
            is AppNavigationState.Space -> {}
            is AppNavigationState.Room -> {
                // Cleanup notification for current room
                onEnteringRoom(appNavigationState.parentSpace.parentSession.sessionId.value, appNavigationState.roomId.value)
            }
            is AppNavigationState.Thread -> {
                onEnteringThread(
                    appNavigationState.parentRoom.parentSpace.parentSession.sessionId.value,
                    appNavigationState.parentRoom.roomId.value,
                    appNavigationState.threadId.value
                )
            }
        }
    }

    private fun createInitialNotificationState(): NotificationState {
        val queuedEvents = notificationEventPersistence.loadEvents(factory = { rawEvents ->
            NotificationEventQueue(rawEvents.toMutableList(), seenEventIds = CircularCache.create(cacheSize = 25))
        })
        val renderedEvents = queuedEvents.rawEvents().map { ProcessedEvent(ProcessedEvent.Type.KEEP, it) }.toMutableList()
        return NotificationState(queuedEvents, renderedEvents)
    }

    /**
    Should be called as soon as a new event is ready to be displayed.
    The notification corresponding to this event will not be displayed until
    #refreshNotificationDrawer() is called.
    Events might be grouped and there might not be one notification per event!
     */
    fun NotificationEventQueue.onNotifiableEventReceived(notifiableEvent: NotifiableEvent) {
        if (!pushDataStore.areNotificationEnabledForDevice()) {
            Timber.i("Notification are disabled for this device")
            return
        }
        // If we support multi session, event list should be per userId
        // Currently only manage single session
        if (buildMeta.lowPrivacyLoggingEnabled) {
            Timber.d("onNotifiableEventReceived(): $notifiableEvent")
        } else {
            Timber.d("onNotifiableEventReceived(): is push: ${notifiableEvent.canBeReplaced}")
        }

        if (filteredEventDetector.shouldBeIgnored(notifiableEvent)) {
            Timber.d("onNotifiableEventReceived(): ignore the event")
            return
        }

        add(notifiableEvent)
    }

    /**
     * Clear all known events and refresh the notification drawer.
     */
    fun clearAllEvents(sessionId: String) {
        updateEvents { it.clearMessagesForSession(sessionId) }
    }

    /**
     * Should be called when the application is currently opened and showing timeline for the given roomId.
     * Used to ignore events related to that room (no need to display notification) and clean any existing notification on this room.
     */
    private fun onEnteringRoom(sessionId: String, roomId: String) {
        updateEvents {
            it.clearMessagesForRoom(sessionId, roomId)
        }
    }

    /**
     * Should be called when the application is currently opened and showing timeline for the given threadId.
     * Used to ignore events related to that thread (no need to display notification) and clean any existing notification on this room.
     */
    private fun onEnteringThread(sessionId: String, roomId: String, threadId: String) {
        updateEvents {
            it.clearMessagesForThread(sessionId, roomId, threadId)
        }
    }

    // TODO EAx Must be per account
    fun notificationStyleChanged() {
        updateEvents {
            val newSettings = pushDataStore.useCompleteNotificationFormat()
            if (newSettings != useCompleteNotificationFormat) {
                // Settings has changed, remove all current notifications
                notificationRenderer.cancelAllNotifications()
                useCompleteNotificationFormat = newSettings
            }
        }
    }

    fun updateEvents(action: NotificationDrawerManager.(NotificationEventQueue) -> Unit) {
        notificationState.updateQueuedEvents(this) { queuedEvents, _ ->
            action(queuedEvents)
        }
        refreshNotificationDrawer()
    }

    private fun refreshNotificationDrawer() {
        // Implement last throttler
        val canHandle = firstThrottler.canHandle()
        Timber.v("refreshNotificationDrawer(), delay: ${canHandle.waitMillis()} ms")
        backgroundHandler.removeCallbacksAndMessages(null)

        backgroundHandler.postDelayed(
            {
                try {
                    refreshNotificationDrawerBg()
                } catch (throwable: Throwable) {
                    // It can happen if for instance session has been destroyed. It's a bit ugly to try catch like this, but it's safer
                    Timber.w(throwable, "refreshNotificationDrawerBg failure")
                }
            },
            canHandle.waitMillis()
        )
    }

    @WorkerThread
    private fun refreshNotificationDrawerBg() {
        Timber.v("refreshNotificationDrawerBg()")
        val eventsToRender = notificationState.updateQueuedEvents(this) { queuedEvents, renderedEvents ->
            notifiableEventProcessor.process(queuedEvents.rawEvents(), currentAppNavigationState, renderedEvents).also {
                queuedEvents.clearAndAdd(it.onlyKeptEvents())
            }
        }

        if (notificationState.hasAlreadyRendered(eventsToRender)) {
            Timber.d("Skipping notification update due to event list not changing")
        } else {
            notificationState.clearAndAddRenderedEvents(eventsToRender)
            renderEvents(eventsToRender)
            persistEvents()
        }
    }

    private fun persistEvents() {
        notificationState.queuedEvents { queuedEvents ->
            notificationEventPersistence.persistEvents(queuedEvents)
        }
    }

    private fun renderEvents(eventsToRender: List<ProcessedEvent<NotifiableEvent>>) {
        // Group by sessionId
        val eventsForSessions = eventsToRender.groupBy {
            it.event.sessionId
        }

        eventsForSessions.forEach { (sessionId, notifiableEvents) ->
            // TODO EAx val user = session.getUserOrDefault(session.myUserId)
            // myUserDisplayName cannot be empty else NotificationCompat.MessagingStyle() will crash
            val myUserDisplayName = "Todo display name" // user.toMatrixItem().getBestName()
            // TODO EAx avatar URL
            val myUserAvatarUrl = null // session.contentUrlResolver().resolveThumbnail(
            //    contentUrl = user.avatarUrl,
            //    width = avatarSize,
            //    height = avatarSize,
            //    method = ContentUrlResolver.ThumbnailMethod.SCALE
            //)
            notificationRenderer.render(sessionId, myUserDisplayName, myUserAvatarUrl, useCompleteNotificationFormat, notifiableEvents)
        }
    }

    fun shouldIgnoreMessageEventInRoom(resolvedEvent: NotifiableMessageEvent): Boolean {
        return resolvedEvent.shouldIgnoreMessageEventInRoom(currentAppNavigationState)
    }
}
