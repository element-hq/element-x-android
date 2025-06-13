/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.compound.theme.ElementTheme
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
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.services.analytics.api.ScreenTracker
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import io.element.android.services.appnavstate.api.AppForegroundStateService
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class CallScreenPresenter @AssistedInject constructor(
    @Assisted private val callType: CallType,
    @Assisted private val navigator: CallScreenNavigator,
    private val callWidgetProvider: CallWidgetProvider,
    userAgentProvider: UserAgentProvider,
    private val clock: SystemClock,
    private val dispatchers: CoroutineDispatchers,
    private val matrixClientsProvider: MatrixClientProvider,
    private val screenTracker: ScreenTracker,
    private val activeCallManager: ActiveCallManager,
    private val languageTagProvider: LanguageTagProvider,
    private val appForegroundStateService: AppForegroundStateService,
    private val activeRoomsHolder: ActiveRoomsHolder,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
) : Presenter<CallScreenState> {
    @AssistedFactory
    interface Factory {
        fun create(callType: CallType, navigator: CallScreenNavigator): CallScreenPresenter
    }

    private val isInWidgetMode = callType is CallType.RoomCall
    private val userAgent = userAgentProvider.provide()
    private var notifiedCallStart = false

    @Composable
    override fun present(): CallScreenState {
        val coroutineScope = rememberCoroutineScope()
        val urlState = remember { mutableStateOf<AsyncData<String>>(AsyncData.Uninitialized) }
        val callWidgetDriver = remember { mutableStateOf<MatrixWidgetDriver?>(null) }
        val messageInterceptor = remember { mutableStateOf<WidgetMessageInterceptor?>(null) }
        var isJoinedCall by rememberSaveable { mutableStateOf(false) }
        var ignoreWebViewError by rememberSaveable { mutableStateOf(false) }
        var webViewError by remember { mutableStateOf<String?>(null) }
        val languageTag = languageTagProvider.provideLanguageTag()
        val theme = if (ElementTheme.isLightTheme) "light" else "dark"
        DisposableEffect(Unit) {
            coroutineScope.launch {
                // Sets the call as joined
                activeCallManager.joinedCall(callType)
                fetchRoomCallUrl(
                    inputs = callType,
                    urlState = urlState,
                    callWidgetDriver = callWidgetDriver,
                    languageTag = languageTag,
                    theme = theme,
                )
            }
            onDispose {
                appCoroutineScope.launch { activeCallManager.hungUpCall(callType) }
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
                        // We are receiving messages from the WebView, consider that the application is loaded
                        ignoreWebViewError = true
                        // Relay message to Widget Driver
                        callWidgetDriver.value?.send(it)

                        val parsedMessage = parseMessage(it)
                        if (parsedMessage?.direction == WidgetMessage.Direction.FromWidget) {
                            if (parsedMessage.action == WidgetMessage.Action.Close) {
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

        fun handleEvents(event: CallScreenEvents) {
            when (event) {
                is CallScreenEvents.Hangup -> {
                    val widgetId = callWidgetDriver.value?.id
                    val interceptor = messageInterceptor.value
                    if (widgetId != null && interceptor != null && isJoinedCall) {
                        // If the call was joined, we need to hang up first. Then the UI will be dismissed automatically.
                        sendHangupMessage(widgetId, interceptor)
                        isJoinedCall = false

                        coroutineScope.launch {
                            // Wait for a couple of seconds to receive the hangup message
                            // If we don't get it in time, we close the screen anyway
                            delay(2.seconds)
                            close(callWidgetDriver.value, navigator)
                        }
                    } else {
                        coroutineScope.launch {
                            close(callWidgetDriver.value, navigator)
                        }
                    }
                }
                is CallScreenEvents.SetupMessageChannels -> {
                    messageInterceptor.value = event.widgetMessageInterceptor
                }
                is CallScreenEvents.OnWebViewError -> {
                    if (!ignoreWebViewError) {
                        webViewError = event.description.orEmpty()
                    }
                    // Else ignore the error, give a chance the Element Call to recover by itself.
                }
            }
        }

        return CallScreenState(
            urlState = urlState.value,
            webViewError = webViewError,
            userAgent = userAgent,
            isCallActive = isJoinedCall,
            isInWidgetMode = isInWidgetMode,
            eventSink = { handleEvents(it) },
        )
    }

    private suspend fun fetchRoomCallUrl(
        inputs: CallType,
        urlState: MutableState<AsyncData<String>>,
        callWidgetDriver: MutableState<MatrixWidgetDriver?>,
        languageTag: String?,
        theme: String?,
    ) {
        urlState.runCatchingUpdatingState {
            when (inputs) {
                is CallType.ExternalUrl -> {
                    inputs.url
                }
                is CallType.RoomCall -> {
                    val result = callWidgetProvider.getWidget(
                        sessionId = inputs.sessionId,
                        roomId = inputs.roomId,
                        clientId = UUID.randomUUID().toString(),
                        languageTag = languageTag,
                        theme = theme,
                    ).getOrThrow()
                    callWidgetDriver.value = result.driver
                    Timber.d("Call widget driver initialized for sessionId: ${inputs.sessionId}, roomId: ${inputs.roomId}")
                    result.url
                }
            }
        }
    }

    @Composable
    private fun HandleMatrixClientSyncState() {
        val coroutineScope = rememberCoroutineScope()
        DisposableEffect(Unit) {
            val roomCallType = callType as? CallType.RoomCall ?: return@DisposableEffect onDispose {}
            val client = matrixClientsProvider.getOrNull(roomCallType.sessionId) ?: return@DisposableEffect onDispose {
                Timber.w("No MatrixClient found for sessionId, can't send call notification: ${roomCallType.sessionId}")
            }
            coroutineScope.launch {
                Timber.d("Observing sync state in-call for sessionId: ${roomCallType.sessionId}")
                client.syncService().syncState
                    .collect { state ->
                        if (state == SyncState.Running) {
                            client.notifyCallStartIfNeeded(callType.roomId)
                        } else {
                            appForegroundStateService.updateIsInCallState(true)
                        }
                    }
            }
            onDispose {
                Timber.d("Stopped observing sync state in-call for sessionId: ${roomCallType.sessionId}")
                // Make sure we mark the call as ended in the app state
                appForegroundStateService.updateIsInCallState(false)
            }
        }
    }

    private suspend fun MatrixClient.notifyCallStartIfNeeded(roomId: RoomId) {
        if (notifiedCallStart) return

        val activeRoomForSession = activeRoomsHolder.getActiveRoomMatching(sessionId, roomId)
        val sendCallNotificationResult = if (activeRoomForSession != null) {
            Timber.d("Notifying call start for room $roomId. Has room call: ${activeRoomForSession.info().hasRoomCall}")
            activeRoomForSession.sendCallNotificationIfNeeded()
        } else {
            // Instantiate the room from the session and roomId and send the notification
            getJoinedRoom(roomId)?.use { room ->
                Timber.d("Notifying call start for room $roomId. Has room call: ${room.info().hasRoomCall}")
                room.sendCallNotificationIfNeeded()
            } ?: run {
                Timber.w("No room found for session $sessionId and room $roomId, skipping call notification.")
                return
            }
        }

        sendCallNotificationResult.fold(
            onSuccess = { notifiedCallStart = true },
            onFailure = { error ->
                Timber.e(error, "Failed to send call notification for room $roomId.")
            }
        )
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
