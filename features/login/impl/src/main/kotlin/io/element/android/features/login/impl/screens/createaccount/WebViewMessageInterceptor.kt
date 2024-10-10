/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.createaccount

import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

class WebViewMessageInterceptor(
    webView: WebView,
    private val debugLog: Boolean,
    private val onOpenExternalUrl: (String) -> Unit,
    private val onMessage: (String) -> Unit,
) {
    companion object {
        // We call both the WebMessageListener and the JavascriptInterface objects in JS with this
        // 'listenerName' so they can both receive the data from the WebView when
        // `${LISTENER_NAME}.postMessage(...)` is called
        const val LISTENER_NAME = "elementX"
    }

    init {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                // We inject this JS code when the page starts loading to attach a message listener to the window.
                view?.evaluateJavascript(
                    """
                        window.addEventListener(
                          "mobileregistrationresponse",
                          (event) => {
                            let json = JSON.stringify(event.detail)
                            ${"console.log('message sent: ' + json);".takeIf { debugLog }}
                            $LISTENER_NAME.postMessage(json);
                          },
                          false,
                        );
                    """.trimIndent(),
                    null
                )
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                request ?: return false
                // Load the URL in a Chrome Custom Tab, and return true to cancel the load
                onOpenExternalUrl(request.url.toString())
                return true
            }
        }

        // Use WebMessageListener if supported, otherwise use JavascriptInterface
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            // Create a WebMessageListener, which will receive messages from the WebView and reply to them
            val webMessageListener = WebViewCompat.WebMessageListener { _, message, _, _, _ ->
                onMessageReceived(message.data)
            }
            WebViewCompat.addWebMessageListener(
                webView,
                LISTENER_NAME,
                setOf("*"),
                webMessageListener
            )
        } else {
            webView.addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun postMessage(json: String?) {
                        onMessageReceived(json)
                    }
                },
                LISTENER_NAME,
            )
        }
    }

    private fun onMessageReceived(json: String?) {
        // Here is where we would handle the messages from the WebView, passing them to the listener
        json?.let { onMessage(it) }
    }
}
