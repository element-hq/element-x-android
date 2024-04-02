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

import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SPACE_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.push.impl.notifications.fake.FakeImageLoaderHolder
import io.element.android.libraries.push.impl.notifications.fake.MockkNotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.MockkRoomGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fake.MockkSummaryGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.NavigationState
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import io.element.android.services.appnavstate.test.aNavigationState
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DefaultNotificationDrawerManagerTest {
    @Test
    fun `clearAllEvents should have no effect when queue is empty`() = runTest {
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager()
        defaultNotificationDrawerManager.clearAllEvents(A_SESSION_ID)
        defaultNotificationDrawerManager.destroy()
    }

    @Test
    fun `cover all APIs`() = runTest {
        // For now just call all the API. Later, add more valuable tests.
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager()
        defaultNotificationDrawerManager.notificationStyleChanged()
        defaultNotificationDrawerManager.clearAllMessagesEvents(A_SESSION_ID, doRender = true)
        defaultNotificationDrawerManager.clearAllMessagesEvents(A_SESSION_ID, doRender = false)
        defaultNotificationDrawerManager.clearEvent(A_SESSION_ID, AN_EVENT_ID, doRender = true)
        defaultNotificationDrawerManager.clearEvent(A_SESSION_ID, AN_EVENT_ID, doRender = false)
        defaultNotificationDrawerManager.clearMessagesForRoom(A_SESSION_ID, A_ROOM_ID, doRender = true)
        defaultNotificationDrawerManager.clearMessagesForRoom(A_SESSION_ID, A_ROOM_ID, doRender = false)
        defaultNotificationDrawerManager.clearMembershipNotificationForSession(A_SESSION_ID)
        defaultNotificationDrawerManager.clearMembershipNotificationForRoom(A_SESSION_ID, A_ROOM_ID, doRender = true)
        defaultNotificationDrawerManager.clearMembershipNotificationForRoom(A_SESSION_ID, A_ROOM_ID, doRender = false)
        defaultNotificationDrawerManager.onNotifiableEventReceived(aNotifiableMessageEvent())
        // Add the same Event again (will be ignored)
        defaultNotificationDrawerManager.onNotifiableEventReceived(aNotifiableMessageEvent())
        defaultNotificationDrawerManager.destroy()
    }

    @Test
    fun `react to applicationStateChange`() = runTest {
        // For now just call all the API. Later, add more valuable tests.
        val appNavigationStateFlow: MutableStateFlow<AppNavigationState> = MutableStateFlow(
            AppNavigationState(
                navigationState = NavigationState.Root,
                isInForeground = true,
            )
        )
        val appNavigationStateService = FakeAppNavigationStateService(appNavigationState = appNavigationStateFlow)
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager(
            appNavigationStateService = appNavigationStateService
        )
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(), isInForeground = true))
        runCurrent()
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(A_SESSION_ID), isInForeground = true))
        runCurrent()
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(A_SESSION_ID, A_SPACE_ID), isInForeground = true))
        runCurrent()
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(A_SESSION_ID, A_SPACE_ID, A_ROOM_ID), isInForeground = true))
        runCurrent()
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(A_SESSION_ID, A_SPACE_ID, A_ROOM_ID, A_THREAD_ID), isInForeground = true))
        runCurrent()
        // Like a user sign out
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(), isInForeground = true))
        runCurrent()
        defaultNotificationDrawerManager.destroy()
    }

    private fun TestScope.createDefaultNotificationDrawerManager(
        appNavigationStateService: AppNavigationStateService = FakeAppNavigationStateService(),
        initialData: List<NotifiableEvent> = emptyList()
    ): DefaultNotificationDrawerManager {
        val context = RuntimeEnvironment.getApplication()
        return DefaultNotificationDrawerManager(
            notifiableEventProcessor = NotifiableEventProcessor(
                outdatedDetector = OutdatedEventDetector(),
                appNavigationStateService = appNavigationStateService
            ),
            notificationRenderer = NotificationRenderer(
                notificationIdProvider = NotificationIdProvider(),
                notificationDisplayer = NotificationDisplayer(context),
                notificationFactory = NotificationFactory(
                    notificationCreator = MockkNotificationCreator().instance,
                    roomGroupMessageCreator = MockkRoomGroupMessageCreator().instance,
                    summaryGroupMessageCreator = MockkSummaryGroupMessageCreator().instance,
                )
            ),
            notificationEventPersistence = InMemoryNotificationEventPersistence(initialData = initialData),
            filteredEventDetector = FilteredEventDetector(),
            appNavigationStateService = appNavigationStateService,
            coroutineScope = this,
            dispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true),
            buildMeta = aBuildMeta(),
            matrixClientProvider = FakeMatrixClientProvider(),
            imageLoaderHolder = FakeImageLoaderHolder(),
        )
    }
}
