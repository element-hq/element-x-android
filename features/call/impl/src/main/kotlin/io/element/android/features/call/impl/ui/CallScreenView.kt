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

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.call.impl.R
import io.element.android.features.call.impl.utils.WebViewWidgetMessageInterceptor
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar

typealias RequestPermissionCallback = (Array<String>) -> Unit

interface CallScreenNavigator {
    fun close()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallScreenView(
    state: CallScreenState,
    requestPermissions: (Array<String>, RequestPermissionCallback) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.element_call)) },
                navigationIcon = {
                    BackButton(
                        imageVector = CompoundIcons.Close(),
                        onClick = { state.eventSink(CallScreenEvents.Hangup) }
                    )
                }
            )
        }
    ) { padding ->
        BackHandler {
            state.eventSink(CallScreenEvents.Hangup)
        }
        CallWebView(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxSize(),
            url = state.urlState,
            userAgent = state.userAgent,
            onPermissionsRequest = { request ->
                val androidPermissions = mapWebkitPermissions(request.resources)
                val callback: RequestPermissionCallback = { request.grant(it) }
                requestPermissions(androidPermissions.toTypedArray(), callback)
            },
            onWebViewCreate = { webView ->
                val interceptor = WebViewWidgetMessageInterceptor(webView)
                state.eventSink(CallScreenEvents.SetupMessageChannels(interceptor))
            }
        )
    }
}

@Composable
private fun CallWebView(
    url: AsyncData<String>,
    userAgent: String,
    onPermissionsRequest: (PermissionRequest) -> Unit,
    onWebViewCreate: (WebView) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("WebView - can't be previewed")
        }
    } else {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                WebView(context).apply {
                    onWebViewCreate(this)
                    setup(userAgent, onPermissionsRequest)
                }
            },
            update = { webView ->
                if (url is AsyncData.Success && webView.url != url.data) {
                    webView.loadUrl(url.data)
                }
            },
            onRelease = { webView ->
                webView.destroy()
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.setup(
    userAgent: String,
    onPermissionsRequested: (PermissionRequest) -> Unit,
) {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    with(settings) {
        javaScriptEnabled = true
        allowContentAccess = true
        allowFileAccess = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false
        databaseEnabled = true
        loadsImagesAutomatically = true
        userAgentString = userAgent
    }

    webChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            onPermissionsRequested(request)
        }
    }
}

@PreviewsDayNight
@Composable
internal fun CallScreenViewPreview() {
    ElementPreview {
        CallScreenView(
            state = CallScreenState(
                urlState = AsyncData.Success("https://call.element.io/some-actual-call?with=parameters"),
                isInWidgetMode = false,
                userAgent = "",
                eventSink = {},
            ),
            requestPermissions = { _, _ -> },
        )
    }
}
