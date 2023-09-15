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

package io.element.android.features.call

import android.Manifest
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kotlin.time.Duration.Companion.minutes

class ElementCallActivity : ComponentActivity() {

    private lateinit var wakeLock: WakeLock
    private lateinit var audioManager: AudioManager
    private var lastMultimediaVolume = 0

    private var webView: WebView? = null
    private lateinit var handler: Handler

    // Example toWidget event to reply to the WebView
    private val exampleToWidgetEvent = "{\"requestId\":\"c22b40aa-6f77-4403-b2af-088cf4b77aac\",\"widgetId\":\"w_id_1234\",\"api\":\"toWidget\",\"action\":\"capabilities\",\"data\":{}}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_call)

        CallForegroundService.start(this)

        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccess = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.databaseEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36"

        this.webView = webView
        handler = Handler(Looper.getMainLooper())

        val powerManager = getSystemService(PowerManager::class.java)
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "elementx:CallWakeLock")
        wakeLock.acquire(10.minutes.inWholeMilliseconds)

        audioManager = getSystemService(AudioManager::class.java)

        var permissionRequest: PermissionRequest? = null
        val requestPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val request = permissionRequest ?: return@registerForActivityResult
            val permissionsToGrant = mutableListOf<String>()
            permissions.forEach { (permission, granted) ->
                if (granted) {
                    val webKitPermission = when (permission) {
                        Manifest.permission.CAMERA -> PermissionRequest.RESOURCE_VIDEO_CAPTURE
                        Manifest.permission.RECORD_AUDIO -> PermissionRequest.RESOURCE_AUDIO_CAPTURE
                        else -> return@forEach
                    }
                    permissionsToGrant.add(webKitPermission)
                }
            }
            request.grant(permissionsToGrant.toTypedArray())
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                permissionRequest = request
                requestPermissionsLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                )
            }
        }

        // We call both the WebMessageListener and the JavascriptInterface objects in JS with this
        // 'listenerName' so they can both receive the data from the WebView when
        // `$listenerName.postMessage` is called
        val listenerName = "elementX"

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                // We inject this JS code when the page starts loading to attach a message listener to the window.
                // This listener will receive both messages:
                // - EC widget API -> Element X (message.data.api == "fromWidget")
                // - Element X -> EC widget API (message.data.api == "toWidget"), we should ignore these
                view?.evaluateJavascript(
                    """
                        console.log("Attaching event listener")
                        window.addEventListener('message', function(event) {
                         let message = {data: event.data, origin: event.origin}
                            if (!message.data.response && message.data.api == "fromWidget") {
                                let json = JSON.stringify(event.data) 
                                console.log('message sent: ' + json);
                                $listenerName.postMessage(json);
                            } else {
                                console.log('message received: ' + JSON.stringify(event.data));
                            }
                        });
                        
                        // This is where the messages from the `replyProxy.postMessage` are received
                        $listenerName.onmessage = function(event) {
                            postMessage(JSON.parse(event.data), '*');
                        }
                    """.trimIndent(),
                    null
                )
            }
        }

        // Create a WebMessageListener, which will receive messages from the WebView and reply to them
        val webMessageListener = object : WebViewCompat.WebMessageListener {
            override fun onPostMessage(
                view: WebView,
                message: WebMessageCompat,
                sourceOrigin: Uri,
                isMainFrame: Boolean,
                replyProxy: JavaScriptReplyProxy
            ) {
                onMessageReceived(message.data)
                replyProxy.postMessage(exampleToWidgetEvent)
            }
        }

        // Use WebMessageListener if supported, otherwise use JavascriptInterface
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(webView, listenerName, setOf("*"), webMessageListener)
            println("$listenerName listener added")
        } else {
            webView.addJavascriptInterface(this, listenerName)
        }

        val userId = "%40jorgem_test%3Amatrix.org"
        val roomId = "%21hjHgOuPWxAvPIQuAkT%3Amatrix.org"
        // Example of 'real' url for EC with widget API
        val url = "https://element-call-livekit.netlify.app/room?widgetId=w_id_1234&parentUrl=*&embed=&preload=&hideHeader=&userId=$userId&deviceId=ONLDUUSMTR&roomId=$roomId&baseUrl=https%3A%2F%2Fmatrix-client.matrix.org&lang=en-us&fontScale=1"

//        val url = "http://192.168.1.120:8080/#/?widgetId=stefan&userId=%40stefan.ceriu%3Amatrix.org"
        webView.loadUrl(url)
    }

    override fun onResume() {
        super.onResume()

        lastMultimediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val minVolume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        } else 0
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, minVolume, 0)
    }

    override fun onPause() {
        super.onPause()

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, lastMultimediaVolume, 0)
    }

    override fun onDestroy() {
        super.onDestroy()

        wakeLock.release()
        CallForegroundService.stop(this)
    }

    // This is where we receive messages from the WebView using the JavascriptInterface
    @JavascriptInterface
    fun postMessage(json: String?) {
        // Do something with the message
        onMessageReceived(json)
        handler.post {
            // We receive a message from the WebView, we send a sample message back to it
            webView?.evaluateJavascript("postMessage($exampleToWidgetEvent, '*')", null)
        }
    }

    private fun onMessageReceived(json: String?) {
        // Here is where we would handle the messages from the WebView, probably passing them to
        // the Rust SDK
        println("onPostMessage: $json")
    }

}
