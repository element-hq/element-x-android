/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.impl.ui

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
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.widget.api.WidgetActivityData
import io.element.android.features.widget.impl.data.WidgetMessage
import io.element.android.features.widget.impl.utils.WidgetMessageInterceptor
import io.element.android.features.widget.impl.utils.WidgetMessageSerializer
import io.element.android.features.widget.impl.utils.WidgetProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class WidgetScreenPresenter(
    @Assisted private val widgetActivityData: WidgetActivityData,
    @Assisted private val navigator: WidgetScreenNavigator,
    private val widgetProvider: WidgetProvider,
    userAgentProvider: UserAgentProvider,
    private val clock: SystemClock,
    private val dispatchers: CoroutineDispatchers,
    private val languageTagProvider: LanguageTagProvider,
    private val widgetMessageSerializer: WidgetMessageSerializer,
) : Presenter<WidgetScreenState> {
    @AssistedFactory
    interface Factory {
        fun create(widgetActivityData: WidgetActivityData, navigator: WidgetScreenNavigator): WidgetScreenPresenter
    }

    private val initAfterContentLoad = false
    private val isInWidgetMode = true
    private val userAgent = userAgentProvider.provide()

    @Composable
    override fun present(): WidgetScreenState {
        val coroutineScope = rememberCoroutineScope()
        val urlState = remember { mutableStateOf<AsyncData<String>>(AsyncData.Uninitialized) }
        val widgetDriver = remember { mutableStateOf<MatrixWidgetDriver?>(null) }
        val messageInterceptor = remember { mutableStateOf<WidgetMessageInterceptor?>(null) }
        var isWidgetLoaded by rememberSaveable { mutableStateOf(false) }
        var ignoreWebViewError by rememberSaveable { mutableStateOf(false) }
        var webViewError by remember { mutableStateOf<String?>(null) }
        val languageTag = languageTagProvider.provideLanguageTag()
        val theme = if (ElementTheme.isLightTheme) "light" else "dark"

        DisposableEffect(Unit) {
            coroutineScope.launch {
                fetchWidgetUrl(
                    inputs = widgetActivityData,
                    urlState = urlState,
                    widgetDriver = widgetDriver,
                    languageTag = languageTag,
                    theme = theme,
                )
            }
            onDispose { }
        }

        widgetDriver.value?.let { driver ->
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
                        widgetDriver.value?.send(it)

                        val parsedMessage = parseMessage(it)
                        val loadedIndicatorWidgetAction = if (initAfterContentLoad) {
                            WidgetMessage.Action.ContentLoaded
                        } else {
                             WidgetMessage.Action.SupportedApiVersions
                        }
                        if (parsedMessage?.direction == WidgetMessage.Direction.FromWidget) {
                            if (parsedMessage.action == WidgetMessage.Action.Close) {
                                close(widgetDriver.value, navigator)
                            } else if (parsedMessage.action == loadedIndicatorWidgetAction) {
                                isWidgetLoaded = true
                            }
                        }
                    }
                    .launchIn(this)
            }

            // Note: For external URLs isWidgetLoaded will always be false
            // We do not need this since we will have the back button in this view
            // can be removed properly after testing
            LaunchedEffect(Unit) {
                // Wait for the widget to load, if it takes too long, we display an error
                delay(30.seconds)

                if (!isWidgetLoaded) {
                    Timber.w("The widget took too long to load. Displaying an error before exiting.")

                    // This will display a simple 'Sorry, an error occurred' dialog and force the user to exit
//                    webViewError = ""
                }
            }
        }

        fun handleEvent(event: WidgetScreenEvents) {
            when (event) {
                is WidgetScreenEvents.Close -> {
                    val widgetId = widgetDriver.value?.id
                    val interceptor = messageInterceptor.value
                    if (widgetId != null && interceptor != null && isWidgetLoaded) {
                        // If the widget was loaded, we need to send a close message first.

                        isWidgetLoaded = false

                        coroutineScope.launch {
                            // Wait for a couple of seconds to receive the close message
                            // If we don't get it in time, we close the screen anyway
                            delay(2.seconds)
                            close(widgetDriver.value, navigator)
                        }
                    } else {
                        coroutineScope.launch {
                            close(widgetDriver.value, navigator)
                        }
                    }
                }
                is WidgetScreenEvents.SetupMessageChannels -> {
                    messageInterceptor.value = event.widgetMessageInterceptor
                }
                is WidgetScreenEvents.OnWebViewError -> {
                    if (!ignoreWebViewError) {
                        webViewError = event.description.orEmpty()
                    }
                    // Else ignore the error, give the widget a chance to recover by itself.
                }
            }
        }

        return WidgetScreenState(
            urlState = urlState.value,
            webViewError = webViewError,
            userAgent = userAgent,
            isWidgetLoaded = isWidgetLoaded,
            isInWidgetMode = isInWidgetMode,
            widgetName = widgetActivityData.widgetName,
            eventSink = ::handleEvent,
        )
    }

    private suspend fun fetchWidgetUrl(
        inputs: WidgetActivityData,
        urlState: MutableState<AsyncData<String>>,
        widgetDriver: MutableState<MatrixWidgetDriver?>,
        languageTag: String?,
        theme: String?,
    ) {
        urlState.runCatchingUpdatingState {
            val result = widgetProvider.getWidget(
                sessionId = inputs.sessionId,
                roomId = inputs.roomId,
                clientId = UUID.randomUUID().toString(),
                languageTag = languageTag,
                theme = theme,
                initAfterContentLoad = initAfterContentLoad,
                url = inputs.url
                ).getOrThrow()

            widgetDriver.value = result.driver
            Timber.d("Widget driver initialized for sessionId: ${inputs.sessionId}, roomId: ${inputs.roomId}")
            result.url
        }
    }

    private fun parseMessage(message: String): WidgetMessage? {
        return widgetMessageSerializer.deserialize(message).getOrNull()
    }

    private fun CoroutineScope.close(widgetDriver: MatrixWidgetDriver?, navigator: WidgetScreenNavigator) = launch(dispatchers.io) {
        navigator.close()
        widgetDriver?.close()
    }
}

