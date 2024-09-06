/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl.webview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.viewinterop.AndroidView
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.oidc.impl.OidcUrlParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OidcView(
    state: OidcState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
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

    fun onBack() {
        if (webView?.canGoBack().orFalse()) {
            webView?.goBack()
        } else {
            // To properly cancel Oidc login
            state.eventSink.invoke(OidcEvents.Cancel)
        }
    }

    BackHandler { onBack() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackButton(onClick = ::onBack)
                },
            )
        }
    ) { contentPadding ->
        AndroidView(
            modifier = Modifier.padding(contentPadding),
            factory = { context ->
                WebView(context).apply {
                    if (!isPreview) {
                        webViewClient = oidcWebViewClient
                        settings.apply {
                            @SuppressLint("SetJavaScriptEnabled")
                            javaScriptEnabled = true
                            allowContentAccess = true
                            allowFileAccess = true
                            databaseEnabled = true
                            domStorageEnabled = true
                        }
                        loadUrl(state.oidcDetails.url)
                    }
                }.also {
                    webView = it
                }
            }
        )

        AsyncActionView(
            async = state.requestState,
            onSuccess = { onNavigateBack() },
            onErrorDismiss = { state.eventSink(OidcEvents.ClearError) }
        )
    }
}

@PreviewsDayNight
@Composable
internal fun OidcViewPreview(@PreviewParameter(OidcStateProvider::class) state: OidcState) = ElementPreview {
    OidcView(
        state = state,
        onNavigateBack = {},
    )
}
