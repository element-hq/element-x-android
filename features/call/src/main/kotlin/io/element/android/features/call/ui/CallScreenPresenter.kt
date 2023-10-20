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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.call.CallType
import io.element.android.features.call.data.WidgetMessage
import io.element.android.features.call.utils.CallWidgetProvider
import io.element.android.features.call.utils.WidgetMessageInterceptor
import io.element.android.features.call.utils.WidgetMessageSerializer
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

class CallScreenPresenter @AssistedInject constructor(
    @Assisted private val callType: CallType,
    @Assisted private val navigator: CallScreenNavigator,
    private val callWidgetProvider: CallWidgetProvider,
    private val userAgentProvider: UserAgentProvider,
    private val clock: SystemClock,
    private val dispatchers: CoroutineDispatchers,
) : Presenter<CallScreenState> {

    @AssistedFactory
    interface Factory {
        fun create(callType: CallType, navigator: CallScreenNavigator): CallScreenPresenter
    }

    private val isInWidgetMode = callType is CallType.RoomCall
    private val userAgent = userAgentProvider.provide()

    @Composable
    override fun present(): CallScreenState {
        val coroutineScope = rememberCoroutineScope()
        val urlState = remember { mutableStateOf<Async<String>>(Async.Uninitialized) }
        val callWidgetDriver = remember { mutableStateOf<MatrixWidgetDriver?>(null) }
        val messageInterceptor = remember { mutableStateOf<WidgetMessageInterceptor?>(null) }

        LaunchedEffect(Unit) {
            loadUrl(callType, urlState, callWidgetDriver)
        }

        callWidgetDriver.value?.let { driver ->
            LaunchedEffect(Unit) {
                driver.incomingMessages
                    .onEach {
                        // Relay message to the WebView
                        messageInterceptor.value?.sendMessage(it)
                    }
                    .launchIn(this)

                driver.run()
            }
        }

        messageInterceptor.value?.let { interceptor ->
            LaunchedEffect(Unit) {
                interceptor.interceptedMessages
                    .onEach {
                        // Relay message to Widget Driver
                        callWidgetDriver.value?.send(it)

                        val parsedMessage = parseMessage(it)
                        if (parsedMessage?.direction == WidgetMessage.Direction.FromWidget && parsedMessage.action == WidgetMessage.Action.HangUp) {
                            close(callWidgetDriver.value, navigator)
                        }
                    }
                    .launchIn(this)
            }
        }

        fun handleEvents(event: CallScreeEvents) {
            when (event) {
                is CallScreeEvents.Hangup -> {
                    val widgetId = callWidgetDriver.value?.id
                    val interceptor = messageInterceptor.value
                    if (widgetId != null && interceptor != null) {
                        sendHangupMessage(widgetId, interceptor)
                    }
                    coroutineScope.launch {
                        close(callWidgetDriver.value, navigator)
                    }
                }
                is CallScreeEvents.SetupMessageChannels -> {
                    messageInterceptor.value = event.widgetMessageInterceptor
                }
            }
        }

        return CallScreenState(
            urlState = urlState.value,
            userAgent = userAgent,
            isInWidgetMode = isInWidgetMode,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.loadUrl(
        inputs: CallType,
        urlState: MutableState<Async<String>>,
        callWidgetDriver: MutableState<MatrixWidgetDriver?>,
    ) = launch {
        urlState.runCatchingUpdatingState {
            when (inputs) {
                is CallType.ExternalUrl -> {
                    inputs.url
                }
                is CallType.RoomCall -> {
                    val (driver, url) = callWidgetProvider.getWidget(
                        sessionId = inputs.sessionId,
                        roomId = inputs.roomId,
                        clientId = UUID.randomUUID().toString(),
                    ).getOrThrow()
                    callWidgetDriver.value = driver
                    url
                }
            }
        }
    }

    private fun parseMessage(message: String): WidgetMessage? {
        return WidgetMessageSerializer.deserialize(message).getOrNull()
    }

    private fun sendHangupMessage(widgetId: String, messageInterceptor: WidgetMessageInterceptor) {
        val message = WidgetMessage(
            direction = WidgetMessage.Direction.ToWidget,
            widgetId = widgetId,
            requestId = "widgetapi-${clock.epochMillis()}",
            action = WidgetMessage.Action.HangUp,
        )
        messageInterceptor.sendMessage(WidgetMessageSerializer.serialize(message))
    }

    private fun CoroutineScope.close(widgetDriver: MatrixWidgetDriver?, navigator: CallScreenNavigator) = launch(dispatchers.io) {
        navigator.close()
        widgetDriver?.close()
    }

}

