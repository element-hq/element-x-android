/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.linknewdevice.impl.LinkNewMobileHandler
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileStep
import kotlinx.coroutines.launch

@Inject
class LinkNewDeviceRootPresenter(
    private val matrixClient: MatrixClient,
    private val linkNewMobileHandler: LinkNewMobileHandler,
) : Presenter<LinkNewDeviceRootState> {
    @Composable
    override fun present(): LinkNewDeviceRootState {
        val coroutineScope = rememberCoroutineScope()
        var isSupported by remember { mutableStateOf<AsyncData<Boolean>>(AsyncData.Uninitialized) }
        var qrCodeData by remember { mutableStateOf<AsyncData<Unit>>(AsyncData.Uninitialized) }

        LaunchedEffect(Unit) {
            matrixClient.canLinkNewDevice().fold(
                onSuccess = { supported ->
                    isSupported = AsyncData.Success(supported)
                },
                onFailure = {
                    isSupported = AsyncData.Failure(it)
                }
            )
        }

        val step by linkNewMobileHandler.stepFlow.collectAsState()

        LaunchedEffect(step) {
            when (val finalStep = step) {
                is LinkMobileStep.Uninitialized -> {
                    // Ignore this step when loading QrCode
                    if (!qrCodeData.isLoading()) {
                        qrCodeData = AsyncData.Uninitialized
                    }
                }
                is LinkMobileStep.CreatingQrCode -> {
                    qrCodeData = AsyncData.Loading()
                }
                is LinkMobileStep.QrReady -> {
                    qrCodeData = AsyncData.Success(Unit)
                }
                is LinkMobileStep.Error -> {
                    qrCodeData = AsyncData.Failure(finalStep.errorType)
                }
                else -> Unit
            }
        }

        fun handleEvent(event: LinkNewDeviceRootEvent) {
            when (event) {
                LinkNewDeviceRootEvent.CloseDialog -> coroutineScope.launch {
                    linkNewMobileHandler.reset()
                }
            }
        }

        return LinkNewDeviceRootState(
            isSupported = isSupported,
            qrCodeData = qrCodeData,
            eventSink = ::handleEvent,
        )
    }
}
