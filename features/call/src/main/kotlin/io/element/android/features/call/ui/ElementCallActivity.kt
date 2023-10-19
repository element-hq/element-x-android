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

package io.element.android.features.call.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.webkit.PermissionRequest
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.IntentCompat
import com.bumble.appyx.core.integrationpoint.NodeComponentActivity
import io.element.android.features.call.CallForegroundService
import io.element.android.features.call.CallType
import io.element.android.features.call.di.CallBindings
import io.element.android.features.call.utils.CallIntentDataParser
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.theme.ElementTheme
import javax.inject.Inject

class ElementCallActivity : NodeComponentActivity(), CallScreenNavigator {
    companion object {
        private const val EXTRA_CALL_WIDGET_SETTINGS = "EXTRA_CALL_WIDGET_SETTINGS"

        fun start(
            context: Context,
            callInputs: CallType,
        ) {
            val intent = Intent(context, ElementCallActivity::class.java).apply {
                putExtra(EXTRA_CALL_WIDGET_SETTINGS, callInputs)
                addFlags(FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    @Inject lateinit var callIntentDataParser: CallIntentDataParser
    @Inject lateinit var presenterFactory: CallScreenPresenter.Factory

    private lateinit var presenter: CallScreenPresenter

    private lateinit var audioManager: AudioManager

    private var requestPermissionCallback: RequestPermissionCallback? = null

    private var audiofocusRequest: AudioFocusRequest? = null
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    private val requestPermissionsLauncher = registerPermissionResultLauncher()

    private var isDarkMode = false
    private val webViewTarget = mutableStateOf<CallType?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applicationContext.bindings<CallBindings>().inject(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setCallType(intent)

        if (savedInstanceState == null) {
            updateUiMode(resources.configuration)
        }

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        requestAudioFocus()

        setContent {
            val state = presenter.present()
            ElementTheme {
                CallScreenView(
                    state = state,
                    requestPermissions = { permissions, callback ->
                        requestPermissionCallback = callback
                        requestPermissionsLauncher.launch(permissions)
                    }
                )
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        updateUiMode(newConfig)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        setCallType(intent)
    }

    override fun onStart() {
        super.onStart()
        CallForegroundService.stop(this)
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing && !isChangingConfigurations) {
            CallForegroundService.start(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseAudioFocus()
        CallForegroundService.stop(this)
    }

    override fun finish() {
        // Also remove the task from recents
        finishAndRemoveTask()
    }

    override fun close() {
        finish()
    }

    private fun setCallType(intent: Intent?) {
        val inputs = intent?.let {
            IntentCompat.getParcelableExtra(it, EXTRA_CALL_WIDGET_SETTINGS, CallType::class.java)
        }
        val intentUrl = intent?.dataString?.let(::parseUrl)
        when {
            // Re-opened the activity but we have no url to load or a cached one, finish the activity
            intent?.dataString == null && inputs == null && webViewTarget.value == null -> finish()
            inputs != null -> {
                webViewTarget.value = inputs
                presenter = presenterFactory.create(inputs, this)
            }
            intentUrl != null -> {
                val fallbackInputs = CallType.ExternalUrl(intentUrl)
                webViewTarget.value = fallbackInputs
                presenter = presenterFactory.create(fallbackInputs, this)
            }
            // Coming back from notification, do nothing
            else -> return
        }
    }

    private fun parseUrl(url: String?): String? = callIntentDataParser.parse(url)

    private fun registerPermissionResultLauncher(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val callback = requestPermissionCallback ?: return@registerForActivityResult
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
            callback(permissionsToGrant.toTypedArray())
        }
    }

    @Suppress("DEPRECATION")
    private fun requestAudioFocus() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .build()
            audioManager.requestAudioFocus(request)
            audiofocusRequest = request
        } else {
            val listener = AudioManager.OnAudioFocusChangeListener { }
            audioManager.requestAudioFocus(
                listener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE,
            )

            audioFocusChangeListener = listener
        }
    }

    @Suppress("DEPRECATION")
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audiofocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioFocusChangeListener?.let { audioManager.abandonAudioFocus(it) }
        }
    }

    private fun updateUiMode(configuration: Configuration) {
        val prevDarkMode = isDarkMode
        val currentNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_YES
        isDarkMode = currentNightMode != 0
        if (prevDarkMode != isDarkMode) {
            if (isDarkMode) {
                window.setBackgroundDrawableResource(android.R.drawable.screen_background_dark)
            } else {
                window.setBackgroundDrawableResource(android.R.drawable.screen_background_light)
            }
        }
    }
}

internal fun mapWebkitPermissions(permissions: Array<String>): List<String> {
    return permissions.mapNotNull { permission ->
        when (permission) {
            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
            else -> null
        }
    }
}
