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

    val stepFlow: StateFlow<LinkMobileStep>
        get() = linkMobileStepFlow.asStateFlow()

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

    fun reset() {
        currentJob?.cancel()
        currentJob = null
        sessionScope.launch {
            linkMobileStepFlow.emit(LinkMobileStep.Uninitialized)
        }
    }

    fun rotateQrCode() {
        createAndStartNewHandler(forRotating = true)
    }

    fun onTooManyRotation() {
        reset()
        sessionScope.launch {
            linkMobileStepFlow.emit(LinkMobileStep.Error(ErrorType.Expired("Too many QR code rotations")))
        }
    }
}
