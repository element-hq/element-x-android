/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.call.impl.pip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.features.call.impl.utils.WebPipApi
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.log.logger.LoggerTag
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("PiP")

class PictureInPicturePresenter @Inject constructor(
    pipSupportProvider: PipSupportProvider,
) : Presenter<PictureInPictureState> {
    private val isPipSupported = pipSupportProvider.isPipSupported()
    private var pipActivity: PipActivity? = null

    @Composable
    override fun present(): PictureInPictureState {
        val coroutineScope = rememberCoroutineScope()
        var isInPictureInPicture by remember { mutableStateOf(false) }
        var webPipApi by remember { mutableStateOf<WebPipApi?>(null) }

        fun handleEvent(event: PictureInPictureEvents) {
            when (event) {
                is PictureInPictureEvents.SetupWebPipApi -> {
                    webPipApi = event.webPipApi
                }
                PictureInPictureEvents.EnterPictureInPicture -> {
                    coroutineScope.launch {
                        switchToPip(webPipApi)
                    }
                }
                is PictureInPictureEvents.OnPictureInPictureModeChanged -> {
                    Timber.tag(loggerTag.value).d("onPictureInPictureModeChanged: ${event.isInPip}")
                    isInPictureInPicture = event.isInPip
                    if (event.isInPip) {
                        webPipApi?.enterPip()
                    } else {
                        webPipApi?.exitPip()
                    }
                }
            }
        }

        return PictureInPictureState(
            supportPip = isPipSupported,
            isInPictureInPicture = isInPictureInPicture,
            eventSink = ::handleEvent,
        )
    }

    fun setPipActivity(pipActivity: PipActivity?) {
        if (isPipSupported) {
            Timber.tag(loggerTag.value).d("Setting PiP params")
            this.pipActivity = pipActivity
            pipActivity?.setPipParams()
        } else {
            Timber.tag(loggerTag.value).d("onCreate: PiP is not supported")
        }
    }

    /**
     * Enters Picture-in-Picture mode, if allowed by Element Call.
     */
    private suspend fun switchToPip(webPipApi: WebPipApi?) {
        if (isPipSupported) {
            if (webPipApi == null) {
                Timber.tag(loggerTag.value).w("webPipApi is not available")
            }
            if (webPipApi == null || webPipApi.canEnterPip()) {
                Timber.tag(loggerTag.value).d("Switch to PiP mode")
                pipActivity?.enterPipMode()
                    ?.also { Timber.tag(loggerTag.value).d("Switch to PiP mode result: $it") }
            } else {
                Timber.tag(loggerTag.value).w("Cannot enter PiP mode, hangup the call")
                pipActivity?.hangUp()
            }
        }
    }
}
