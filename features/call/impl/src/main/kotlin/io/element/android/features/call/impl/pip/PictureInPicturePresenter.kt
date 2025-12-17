/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.pip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.call.impl.utils.PipController
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.log.logger.LoggerTag
import kotlinx.coroutines.launch
import timber.log.Timber

private val loggerTag = LoggerTag("PiP")

@Inject
class PictureInPicturePresenter(
    pipSupportProvider: PipSupportProvider,
) : Presenter<PictureInPictureState> {
    private val isPipSupported = pipSupportProvider.isPipSupported()
    private var pipView: PipView? = null

    @Composable
    override fun present(): PictureInPictureState {
        val coroutineScope = rememberCoroutineScope()
        var isInPictureInPicture by remember { mutableStateOf(false) }
        var pipController by remember { mutableStateOf<PipController?>(null) }

        fun handleEvent(event: PictureInPictureEvents) {
            when (event) {
                is PictureInPictureEvents.SetPipController -> {
                    pipController = event.pipController
                }
                PictureInPictureEvents.EnterPictureInPicture -> {
                    coroutineScope.launch {
                        switchToPip(pipController)
                    }
                }
                is PictureInPictureEvents.OnPictureInPictureModeChanged -> {
                    Timber.tag(loggerTag.value).d("onPictureInPictureModeChanged: ${event.isInPip}")
                    isInPictureInPicture = event.isInPip
                    if (event.isInPip) {
                        pipController?.enterPip()
                    } else {
                        pipController?.exitPip()
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

    fun setPipView(pipView: PipView?) {
        if (isPipSupported) {
            Timber.tag(loggerTag.value).d("Setting PiP params")
            this.pipView = pipView
            pipView?.setPipParams()
        } else {
            Timber.tag(loggerTag.value).d("setPipView: PiP is not supported")
        }
    }

    /**
     * Enters Picture-in-Picture mode, if allowed by Element Call.
     */
    private suspend fun switchToPip(pipController: PipController?) {
        if (isPipSupported) {
            if (pipController == null) {
                Timber.tag(loggerTag.value).w("webPipApi is not available")
            }
            if (pipController == null || pipController.canEnterPip()) {
                Timber.tag(loggerTag.value).d("Switch to PiP mode")
                pipView?.enterPipMode()
                    ?.also { Timber.tag(loggerTag.value).d("Switch to PiP mode result: $it") }
            } else {
                Timber.tag(loggerTag.value).w("Cannot enter PiP mode, hangup the call")
                pipView?.hangUp()
            }
        }
    }
}
