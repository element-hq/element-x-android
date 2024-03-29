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

package io.element.android.features.call.ui

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.CallType
import io.element.android.features.call.utils.FakeCallWidgetProvider
import io.element.android.features.call.utils.FakeWidgetMessageInterceptor
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.widget.FakeWidgetDriver
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilTimeout
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CallScreenPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - with CallType ExternalUrl just loads the URL`() = runTest {
        val presenter = createCallScreenPresenter(CallType.ExternalUrl("https://call.element.io"))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Wait until the URL is loaded
            skipItems(1)

            val initialState = awaitItem()
            assertThat(initialState.urlState).isEqualTo(AsyncData.Success("https://call.element.io"))
            assertThat(initialState.isInWidgetMode).isFalse()
        }
    }

    @Test
    fun `present - with CallType RoomCall loads URL and runs WidgetDriver`() = runTest {
        val widgetDriver = FakeWidgetDriver()
        val widgetProvider = FakeCallWidgetProvider(widgetDriver)
        val presenter = createCallScreenPresenter(
            callType = CallType.RoomCall(A_SESSION_ID, A_ROOM_ID),
            widgetDriver = widgetDriver,
            widgetProvider = widgetProvider,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Wait until the URL is loaded
            skipItems(1)

            val initialState = awaitItem()
            assertThat(initialState.urlState).isInstanceOf(AsyncData.Success::class.java)
            assertThat(initialState.isInWidgetMode).isTrue()
            assertThat(widgetProvider.getWidgetCalled).isTrue()
            assertThat(widgetDriver.runCalledCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - set message interceptor, send and receive messages`() = runTest {
        val widgetDriver = FakeWidgetDriver()
        val presenter = createCallScreenPresenter(
            callType = CallType.RoomCall(A_SESSION_ID, A_ROOM_ID),
            widgetDriver = widgetDriver,
        )
        val messageInterceptor = FakeWidgetMessageInterceptor()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(CallScreenEvents.SetupMessageChannels(messageInterceptor))

            // And incoming message from the Widget Driver is passed to the WebView
            widgetDriver.givenIncomingMessage("A message")
            assertThat(messageInterceptor.sentMessages).containsExactly("A message")

            // And incoming message from the WebView is passed to the Widget Driver
            messageInterceptor.givenInterceptedMessage("A reply")
            assertThat(widgetDriver.sentMessages).containsExactly("A reply")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - hang up event closes the screen and stops the widget driver`() = runTest(UnconfinedTestDispatcher()) {
        val navigator = FakeCallScreenNavigator()
        val widgetDriver = FakeWidgetDriver()
        val presenter = createCallScreenPresenter(
            callType = CallType.RoomCall(A_SESSION_ID, A_ROOM_ID),
            widgetDriver = widgetDriver,
            navigator = navigator,
            dispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true),
        )
        val messageInterceptor = FakeWidgetMessageInterceptor()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(CallScreenEvents.SetupMessageChannels(messageInterceptor))

            initialState.eventSink(CallScreenEvents.Hangup)

            // Let background coroutines run
            runCurrent()

            assertThat(navigator.closeCalled).isTrue()
            assertThat(widgetDriver.closeCalledCount).isEqualTo(1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - a received hang up message closes the screen and stops the widget driver`() = runTest(UnconfinedTestDispatcher()) {
        val navigator = FakeCallScreenNavigator()
        val widgetDriver = FakeWidgetDriver()
        val presenter = createCallScreenPresenter(
            callType = CallType.RoomCall(A_SESSION_ID, A_ROOM_ID),
            widgetDriver = widgetDriver,
            navigator = navigator,
            dispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true),
        )
        val messageInterceptor = FakeWidgetMessageInterceptor()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(CallScreenEvents.SetupMessageChannels(messageInterceptor))

            messageInterceptor.givenInterceptedMessage("""{"action":"im.vector.hangup","api":"fromWidget","widgetId":"1","requestId":"1"}""")

            // Let background coroutines run
            runCurrent()

            assertThat(navigator.closeCalled).isTrue()
            assertThat(widgetDriver.closeCalledCount).isEqualTo(1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - automatically starts the Matrix client sync when on RoomCall`() = runTest {
        val navigator = FakeCallScreenNavigator()
        val widgetDriver = FakeWidgetDriver()
        val matrixClient = FakeMatrixClient()
        val presenter = createCallScreenPresenter(
            callType = CallType.RoomCall(A_SESSION_ID, A_ROOM_ID),
            widgetDriver = widgetDriver,
            navigator = navigator,
            dispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true),
            matrixClientsProvider = FakeMatrixClientProvider(getClient = { Result.success(matrixClient) })
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            consumeItemsUntilTimeout()

            assertThat(matrixClient.syncService().syncState.value).isEqualTo(SyncState.Running)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - automatically stops the Matrix client sync on dispose`() = runTest {
        val navigator = FakeCallScreenNavigator()
        val widgetDriver = FakeWidgetDriver()
        val matrixClient = FakeMatrixClient()
        val presenter = createCallScreenPresenter(
            callType = CallType.RoomCall(A_SESSION_ID, A_ROOM_ID),
            widgetDriver = widgetDriver,
            navigator = navigator,
            dispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true),
            matrixClientsProvider = FakeMatrixClientProvider(getClient = { Result.success(matrixClient) })
        )
        val hasRun = Mutex(true)
        val job = launch {
            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.collect {
                hasRun.unlock()
            }
        }

        hasRun.lock()

        job.cancelAndJoin()

        assertThat(matrixClient.syncService().syncState.value).isEqualTo(SyncState.Terminated)
    }

    private fun TestScope.createCallScreenPresenter(
        callType: CallType,
        navigator: CallScreenNavigator = FakeCallScreenNavigator(),
        widgetDriver: FakeWidgetDriver = FakeWidgetDriver(),
        widgetProvider: FakeCallWidgetProvider = FakeCallWidgetProvider(widgetDriver),
        dispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        matrixClientsProvider: FakeMatrixClientProvider = FakeMatrixClientProvider(),
    ): CallScreenPresenter {
        val userAgentProvider = object : UserAgentProvider {
            override fun provide(): String {
                return "Test"
            }
        }
        val clock = SystemClock { 0 }
        return CallScreenPresenter(
            callType,
            navigator,
            widgetProvider,
            userAgentProvider,
            clock,
            dispatchers,
            matrixClientsProvider,
            this,
        )
    }
}
