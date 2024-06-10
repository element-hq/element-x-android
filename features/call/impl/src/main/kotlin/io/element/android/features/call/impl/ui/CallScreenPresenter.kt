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

package io.element.android.features.call.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.data.WidgetMessage
import io.element.android.features.call.impl.utils.ActiveCallManager
import io.element.android.features.call.impl.utils.CallWidgetProvider
import io.element.android.features.call.impl.utils.WidgetMessageInterceptor
import io.element.android.features.call.impl.utils.WidgetMessageSerializer
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.services.analytics.api.ScreenTracker
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

class CallScreenPresenter @AssistedInject constructor(
    @Assisted private val callType: CallType,
    @Assisted private val navigator: CallScreenNavigator,
    private val callWidgetProvider: CallWidgetProvider,
    userAgentProvider: UserAgentProvider,
    private val clock: SystemClock,
    private val dispatchers: CoroutineDispatchers,
    private val matrixClientsProvider: MatrixClientProvider,
    private val screenTracker: ScreenTracker,
    private val appCoroutineScope: CoroutineScope,
    private val activeCallManager: ActiveCallManager,
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
        val urlState = remember { mutableStateOf<AsyncData<String>>(AsyncData.Uninitialized) }
        val callWidgetDriver = remember { mutableStateOf<MatrixWidgetDriver?>(null) }
        val messageInterceptor = remember { mutableStateOf<WidgetMessageInterceptor?>(null) }
        var isJoinedCall by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            loadUrl(callType, urlState, callWidgetDriver)

            if (callType is CallType.RoomCall) {
                activeCallManager.joinedCall(callType.sessionId, callType.roomId)
            }
        }

        when (callType) {
            is CallType.ExternalUrl -> {
                // No analytics yet for external calls
            }
            is CallType.RoomCall -> {
                screenTracker.TrackScreen(screen = MobileScreen.ScreenName.RoomCall)
            }
        }

        HandleMatrixClientSyncState()

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
                        if (parsedMessage?.direction == WidgetMessage.Direction.FromWidget) {
                            if (parsedMessage.action == WidgetMessage.Action.HangUp) {
                                close(callWidgetDriver.value, navigator)
                            } else if (parsedMessage.action == WidgetMessage.Action.SendEvent) {
                                // This event is received when a member joins the call, the first one will be the current one
                                val type = parsedMessage.data?.jsonObject?.get("type")?.jsonPrimitive?.contentOrNull
                                if (type == "org.matrix.msc3401.call.member") {
                                    isJoinedCall = true
                                }
                            }
                        }
                    }
                    .launchIn(this)
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                if (callType is CallType.RoomCall) {
                    activeCallManager.hungUpCall()
                }
            }
        }

        fun handleEvents(event: CallScreenEvents) {
            when (event) {
                is CallScreenEvents.Hangup -> {
                    val widgetId = callWidgetDriver.value?.id
                    val interceptor = messageInterceptor.value
                    if (widgetId != null && interceptor != null && isJoinedCall) {
                        // If the call was joined, we need to hang up first. Then the UI will be dismissed automatically.
                        sendHangupMessage(widgetId, interceptor)
                        isJoinedCall = false
                    } else {
                        coroutineScope.launch {
                            close(callWidgetDriver.value, navigator)
                        }
                    }
                }
                is CallScreenEvents.SetupMessageChannels -> {
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
        urlState: MutableState<AsyncData<String>>,
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

    @Composable
    private fun HandleMatrixClientSyncState() {
        val coroutineScope = rememberCoroutineScope()
        DisposableEffect(Unit) {
            val client = (callType as? CallType.RoomCall)?.sessionId?.let {
                matrixClientsProvider.getOrNull(it)
            } ?: return@DisposableEffect onDispose { }
            coroutineScope.launch {
                client.syncService().syncState
                    .onEach { state ->
                        if (state != SyncState.Running) {
                            client.syncService().startSync()
                        }
                    }
                    .collect()
            }
            onDispose {
                // We can't use the local coroutine scope here because it will be disposed before this effect
                appCoroutineScope.launch {
                    client.syncService().run {
                        if (syncState.value == SyncState.Running) {
                            stopSync()
                        }
                    }
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
            data = null,
        )
        messageInterceptor.sendMessage(WidgetMessageSerializer.serialize(message))
    }

    private fun CoroutineScope.close(widgetDriver: MatrixWidgetDriver?, navigator: CallScreenNavigator) = launch(dispatchers.io) {
        navigator.close()
        widgetDriver?.close()
    }
}
