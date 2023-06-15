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

package io.element.android.features.login.impl.oidc.webview

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.viewinterop.AndroidView
import io.element.android.features.login.impl.oidc.OidcUrlParser
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator

@Composable
fun OidcView(
    state: OidcState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val oidcUrlParser = remember { OidcUrlParser() }
    var webView by remember { mutableStateOf<WebView?>(null) }
    fun shouldOverrideUrl(url: String): Boolean {
        val action = oidcUrlParser.parse(url)
        if (action != null) {
            state.eventSink.invoke(OidcEvents.OidcActionEvent(action))
            return true
        }
        return false
    }

    val oidcWebViewClient = remember {
        OidcWebViewClient(::shouldOverrideUrl)
    }

    BackHandler {
        if (webView?.canGoBack().orFalse()) {
            webView?.goBack()
        } else {
            // To properly cancel Oidc login
            state.eventSink.invoke(OidcEvents.Cancel)
        }
    }

    Box(modifier = modifier.statusBarsPadding()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = oidcWebViewClient
                    loadUrl(state.oidcDetails.url)
                }.also {
                    webView = it
                }
            }
        )

        when (state.requestState) {
            Async.Uninitialized -> Unit
            is Async.Failure -> {
                ErrorDialog(
                    content = state.requestState.exception.toString(),
                    onDismiss = { state.eventSink(OidcEvents.ClearError) }
                )
            }
            is Async.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is Async.Success -> onNavigateBack()
        }
    }
}

@Preview
@Composable
fun OidcViewLightPreview(@PreviewParameter(OidcStateProvider::class) state: OidcState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun OidcViewDarkPreview(@PreviewParameter(OidcStateProvider::class) state: OidcState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: OidcState) {
    OidcView(
        state = state,
        onNavigateBack = { },
    )
}
