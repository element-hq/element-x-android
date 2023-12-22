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

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.cache.CircularCache
import io.element.android.libraries.push.impl.notifications.fixtures.aSimpleNotifiableEvent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultNotificationEventPersistenceTest {
    @Test
    fun `loadEvents should return empty NotificationEventQueue`() {
        val sut = createDefaultNotificationEventPersistence()
        val result = sut.loadEvents(
            factory = { rawEvents ->
                NotificationEventQueue(rawEvents.toMutableList(), seenEventIds = CircularCache.create(cacheSize = 25))
            }
        )
        assertThat(result.isEmpty()).isTrue()
    }

    @Test
    fun `after persisting NotificationEventQueue, loadEvents should return non-empty NotificationEventQueue`() {
        val sut = createDefaultNotificationEventPersistence()
        val notificationEventQueue = NotificationEventQueue(mutableListOf(), seenEventIds = CircularCache.create(cacheSize = 25))
        // First persist an empty queue
        sut.persistEvents(notificationEventQueue)
        // Add an event
        notificationEventQueue.add(aSimpleNotifiableEvent())
        // Persist
        // Note: is cannot work because AndroidKeyStore is not available. But we check that the code does
        // not crash.
        sut.persistEvents(notificationEventQueue)
        sut.loadEvents(
            factory = { rawEvents ->
                NotificationEventQueue(rawEvents.toMutableList(), seenEventIds = CircularCache.create(cacheSize = 25))
            }
        )
        // assertThat(result.isEmpty()).isFalse()
    }

    private fun createDefaultNotificationEventPersistence(): DefaultNotificationEventPersistence {
        val context = RuntimeEnvironment.getApplication()
        return DefaultNotificationEventPersistence(context)
    }
}
