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

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.log.logger.LoggerTag
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

private val loggerTag = LoggerTag("PiP")

class PictureInPicturePresenter @Inject constructor(
    pipSupportProvider: PipSupportProvider,
) : Presenter<PictureInPictureState> {
    private val isPipSupported = pipSupportProvider.isPipSupported()
    private var isInPictureInPicture = mutableStateOf(false)
    private var hostActivity: WeakReference<Activity>? = null

    @Composable
    override fun present(): PictureInPictureState {
        fun handleEvent(event: PictureInPictureEvents) {
            when (event) {
                PictureInPictureEvents.EnterPictureInPicture -> switchToPip()
            }
        }

        return PictureInPictureState(
            supportPip = isPipSupported,
            isInPictureInPicture = isInPictureInPicture.value,
            eventSink = ::handleEvent,
        )
    }

    fun onCreate(activity: Activity) {
        if (isPipSupported) {
            Timber.tag(loggerTag.value).d("onCreate: Setting PiP params")
            hostActivity = WeakReference(activity)
            hostActivity?.get()?.setPictureInPictureParams(getPictureInPictureParams())
        } else {
            Timber.tag(loggerTag.value).d("onCreate: PiP is not supported")
        }
    }

    fun onDestroy() {
        Timber.tag(loggerTag.value).d("onDestroy")
        hostActivity?.clear()
        hostActivity = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPictureInPictureParams(): PictureInPictureParams {
        return PictureInPictureParams.Builder()
            // Portrait for calls seems more appropriate
            .setAspectRatio(Rational(3, 5))
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setAutoEnterEnabled(true)
                }
            }
            .build()
    }

    /**
     * Enters Picture-in-Picture mode.
     */
    private fun switchToPip() {
        if (isPipSupported) {
            Timber.tag(loggerTag.value).d("Switch to PiP mode")
            hostActivity?.get()?.enterPictureInPictureMode(getPictureInPictureParams())
                ?.also { Timber.tag(loggerTag.value).d("Switch to PiP mode result: $it") }
        }
    }

    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        Timber.tag(loggerTag.value).d("onPictureInPictureModeChanged: $isInPictureInPictureMode")
        isInPictureInPicture.value = isInPictureInPictureMode
    }

    fun onUserLeaveHint() {
        Timber.tag(loggerTag.value).d("onUserLeaveHint")
        switchToPip()
    }
}
