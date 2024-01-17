/*
 * Copyright (c) 2021 New Vector Ltd
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

import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.shouldIgnoreEventInRoom
import io.element.android.services.appnavstate.api.AppNavigationStateService
import timber.log.Timber
import javax.inject.Inject

private typealias ProcessedEvents = List<ProcessedEvent<NotifiableEvent>>

private val loggerTag = LoggerTag("NotifiableEventProcessor", LoggerTag.NotificationLoggerTag)

class NotifiableEventProcessor @Inject constructor(
    private val outdatedDetector: OutdatedEventDetector,
    private val appNavigationStateService: AppNavigationStateService,
) {
    fun process(
        queuedEvents: List<NotifiableEvent>,
        renderedEvents: ProcessedEvents,
    ): ProcessedEvents {
        val appState = appNavigationStateService.appNavigationState.value
        val processedEvents = queuedEvents.map {
            val type = when (it) {
                is InviteNotifiableEvent -> ProcessedEvent.Type.KEEP
                is NotifiableMessageEvent -> when {
                    it.shouldIgnoreEventInRoom(appState) -> {
                        ProcessedEvent.Type.REMOVE
                            .also { Timber.tag(loggerTag.value).d("notification message removed due to currently viewing the same room or thread") }
                    }
                    outdatedDetector.isMessageOutdated(it) -> ProcessedEvent.Type.REMOVE
                        .also { Timber.tag(loggerTag.value).d("notification message removed due to being read") }
                    else -> ProcessedEvent.Type.KEEP
                }
                is SimpleNotifiableEvent -> when (it.type) {
                    EventType.REDACTION -> ProcessedEvent.Type.REMOVE
                    else -> ProcessedEvent.Type.KEEP
                }
                is FallbackNotifiableEvent -> when {
                    it.shouldIgnoreEventInRoom(appState) -> {
                        ProcessedEvent.Type.REMOVE
                            .also { Timber.tag(loggerTag.value).d("notification fallback removed due to currently viewing the same room or thread") }
                    }
                    else -> ProcessedEvent.Type.KEEP
                }
            }
            ProcessedEvent(type, it)
        }

        val removedEventsDiff = renderedEvents.filter { renderedEvent ->
            queuedEvents.none { it.eventId == renderedEvent.event.eventId }
        }.map { ProcessedEvent(ProcessedEvent.Type.REMOVE, it.event) }

        return removedEventsDiff + processedEvents
    }
}
