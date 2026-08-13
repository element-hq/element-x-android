/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.linknewdevice.ErrorType
import io.element.android.libraries.matrix.api.linknewdevice.LinkDesktopHandler
import io.element.android.libraries.matrix.api.linknewdevice.LinkDesktopStep
import io.element.android.libraries.matrix.api.logs.LoggerTags
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes

private val loggerTag = LoggerTag("LinkNewDesktopHandler", LoggerTags.linkNewDevice)

@Inject
@SingleIn(SessionScope::class)
class LinkNewDesktopHandler(
    private val matrixClient: MatrixClient,
) {
    private val sessionScope = matrixClient.sessionCoroutineScope
    private val linkDesktopStepFlow = MutableStateFlow<LinkDesktopStep>(
        LinkDesktopStep.Uninitialized
    )

    val stepFlow: StateFlow<LinkDesktopStep>
        get() = linkDesktopStepFlow.asStateFlow()

    private var currentJob: Job? = null
    private var timerJob: Job? = null
    private var handler: LinkDesktopHandler? = null

    private fun resetJobs() {
        currentJob?.cancel()
        currentJob = null
        timerJob?.cancel()
        timerJob = null
    }

    fun createNewHandler() {
        resetJobs()
        handler = matrixClient.createLinkDesktopHandler().getOrNull()
        timerJob = sessionScope.launch {
            delay(2.minutes)
            linkDesktopStepFlow.emit(LinkDesktopStep.Error(ErrorType.Expired("Scanning QrCode took too long.")))
        }
    }

    fun reset() {
        resetJobs()
        sessionScope.launch {
            linkDesktopStepFlow.emit(LinkDesktopStep.Uninitialized)
        }
    }

    fun onScannedCode(data: ByteArray) {
        resetJobs()
        val currentHandler = handler
        if (currentHandler == null) {
            Timber.tag(loggerTag.value).e("onScannedCode: Handler is not initialized. Call createNewHandler() first.")
        } else {
            currentJob = matrixClient.sessionCoroutineScope.launch {
                currentHandler.linkDesktopStep.onEach {
                    linkDesktopStepFlow.emit(it)
                }.launchIn(this)
                currentHandler.handleScannedQrCode(data)
            }
        }
    }
}
