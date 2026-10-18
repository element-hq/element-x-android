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
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileHandler
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileStep
import io.element.android.libraries.matrix.api.logs.LoggerTags
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

private val loggerTag = LoggerTag("LinkNewMobileHandler", LoggerTags.linkNewDevice)

@Inject
@SingleIn(SessionScope::class)
class LinkNewMobileHandler(
    private val matrixClient: MatrixClient,
) {
    private val sessionScope = matrixClient.sessionCoroutineScope
    private var currentJob: Job? = null
    private var handler: LinkMobileHandler? = null

    private val linkMobileStepFlow = MutableStateFlow<LinkMobileStep>(
        LinkMobileStep.Uninitialized
    )

    /**
     * A [StateFlow] that emits the current step of the link mobile process.
     * It starts with [LinkMobileStep.Uninitialized] and will emit other steps as they occur.
     */
    val stepFlow: StateFlow<LinkMobileStep>
        get() = linkMobileStepFlow.asStateFlow()

    /**
     * Creates a new [LinkMobileHandler] and starts the linking process.
     * If [forRotating] is true, it indicates that this is a rotation of the QR code.
     *
     * @param forRotating Whether this is a rotation of the QR code. Defaults to false.
     */
    fun createAndStartNewHandler(forRotating: Boolean = false) {
        Timber.tag(loggerTag.value).d("createAndStartNewHandler()")
        currentJob?.cancel()
        handler = matrixClient.createLinkMobileHandler().getOrNull()
        handler?.let { h ->
            currentJob = sessionScope.launch {
                if (!forRotating) {
                    linkMobileStepFlow.emit(LinkMobileStep.CreatingQrCode)
                }
                h.linkMobileStep
                    .onEach {
                        linkMobileStepFlow.emit(it)
                    }
                    .launchIn(this)
                h.start()
            }
        }
    }

    /**
     * Resets the handler and cancels any ongoing job.
     * This will also emit [LinkMobileStep.Uninitialized] to the [stepFlow].
     */
    fun reset() {
        currentJob?.cancel()
        currentJob = null
        sessionScope.launch {
            linkMobileStepFlow.emit(LinkMobileStep.Uninitialized)
        }
    }

    /**
     * Rotates the QR code by creating a new handler and starting the linking process.
     * This is typically called when a timeout occurs for the current QR code.
     */
    fun rotateQrCode() {
        createAndStartNewHandler(forRotating = true)
    }

    /**
     * Handles the case where there are too many QR code rotations.
     * This will reset the handler and emit an error to the [stepFlow].
     */
    fun onTooManyRotation() {
        reset()
        sessionScope.launch {
            linkMobileStepFlow.emit(LinkMobileStep.Error(ErrorType.Expired("Too many QR code rotations")))
        }
    }
}
