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

package io.element.android.features.call.impl.utils

import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import io.element.android.features.call.impl.BuildConfig
import kotlinx.coroutines.flow.MutableSharedFlow

class WebViewWidgetMessageInterceptor(
    private val webView: WebView,
) : WidgetMessageInterceptor {
    companion object {
        // We call both the WebMessageListener and the JavascriptInterface objects in JS with this
        // 'listenerName' so they can both receive the data from the WebView when
        // `${LISTENER_NAME}.postMessage(...)` is called
        const val LISTENER_NAME = "elementX"
    }

    // It's important to have extra capacity here to make sure we don't drop any messages
    override val interceptedMessages = MutableSharedFlow<String>(extraBufferCapacity = 10)

    init {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                // We inject this JS code when the page starts loading to attach a message listener to the window.
                // This listener will receive both messages:
                // - EC widget API -> Element X (message.data.api == "fromWidget")
                // - Element X -> EC widget API (message.data.api == "toWidget"), we should ignore these
                view?.evaluateJavascript(
                    """
                        window.addEventListener('message', function(event) {
                            let message = {data: event.data, origin: event.origin}
                            if (message.data.response && message.data.api == "toWidget"
                                || !message.data.response && message.data.api == "fromWidget") {
                                let json = JSON.stringify(event.data) 
                                ${"console.log('message sent: ' + json);".takeIf { BuildConfig.DEBUG } }
                                $LISTENER_NAME.postMessage(json);
                            } else {
                                ${"console.log('message received (ignored): ' + JSON.stringify(event.data));".takeIf { BuildConfig.DEBUG } }
                            }
                        });
                    """.trimIndent(),
                    null
                )
            }
        }

        // Create a WebMessageListener, which will receive messages from the WebView and reply to them
        val webMessageListener = WebViewCompat.WebMessageListener { _, message, _, _, _ ->
            onMessageReceived(message.data)
        }

        // Use WebMessageListener if supported, otherwise use JavascriptInterface
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(
                webView,
                LISTENER_NAME,
                setOf("*"),
                webMessageListener
            )
        } else {
            webView.addJavascriptInterface(object {
                @JavascriptInterface
                fun postMessage(json: String?) {
                    onMessageReceived(json)
                }
            }, LISTENER_NAME)
        }
    }

    override fun sendMessage(message: String) {
        webView.evaluateJavascript("postMessage($message, '*')", null)
    }

    private fun onMessageReceived(json: String?) {
        // Here is where we would handle the messages from the WebView, passing them to the Rust SDK
        json?.let { interceptedMessages.tryEmit(it) }
    }
}
