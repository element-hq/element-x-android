/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.createaccount

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.impl.R
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.theme.progressIndicatorTrackColor
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountView(
    state: CreateAccountState,
    onBackClick: () -> Unit,
    onOpenExternalUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = stringResource(R.string.screen_create_account_title),
                navigationIcon = {
                    BackButton(onClick = onBackClick)
                },
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding)
                .fillMaxSize()
        ) {
            CreateAccountWebView(
                modifier = Modifier
                    .fillMaxSize(),
                state = state,
                onWebViewCreate = { webView ->
                    WebViewMessageInterceptor(
                        webView,
                        state.isDebugBuild,
                        onOpenExternalUrl = onOpenExternalUrl,
                        onMessage = {
                            state.eventSink(CreateAccountEvents.OnMessageReceived(it))
                        },
                    )
                }
            )
            AnimatedVisibility(
                visible = state.pageProgress != 100,
                // Disable enter animation
                enter = fadeIn(initialAlpha = 1f),
                exit = fadeOut(),
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    progress = { state.pageProgress / 100f },
                    trackColor = ElementTheme.colors.progressIndicatorTrackColor,
                )
            }
        }
    }

    AsyncActionView(
        async = state.createAction,
        onSuccess = {},
        onErrorDismiss = onBackClick,
        onRetry = null
    )
}

@Composable
private fun CreateAccountWebView(
    state: CreateAccountState,
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
                    setup(state)
                }
            },
            update = { webView ->
                if (webView.url != state.url) {
                    webView.loadUrl(state.url)
                }
            },
            onRelease = { webView ->
                webView.destroy()
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.setup(state: CreateAccountState) {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    with(settings) {
        javaScriptEnabled = true
        domStorageEnabled = true
    }

    webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            state.eventSink(CreateAccountEvents.SetPageProgress(newProgress))
        }

        override fun onJsBeforeUnload(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            Timber.w("onJsBeforeUnload, cancelling the dialog, we will open external links in a Custom Chrome Tab")
            result?.confirm()
            return true
        }
    }
}

@PreviewsDayNight
@Composable
internal fun CreateAccountViewPreview(@PreviewParameter(CreateAccountStateProvider::class) state: CreateAccountState) = ElementPreview {
    CreateAccountView(
        state = state,
        onBackClick = {},
        onOpenExternalUrl = {},
    )
}
