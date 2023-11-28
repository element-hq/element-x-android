/*
 * Copyright (c) 2023 New Vector Ltd
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

import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent

class InMemoryNotificationEventPersistence(
    initialData: List<NotifiableEvent> = emptyList()
) : NotificationEventPersistence {
    private var data: List<NotifiableEvent> = initialData

    override fun loadEvents(factory: (List<NotifiableEvent>) -> NotificationEventQueue): NotificationEventQueue {
        return factory(data)
    }

    override fun persistEvents(queuedEvents: NotificationEventQueue) {
        data = queuedEvents.rawEvents()
    }
}
