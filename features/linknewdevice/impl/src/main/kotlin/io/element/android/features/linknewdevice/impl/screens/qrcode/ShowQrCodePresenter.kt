/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.qrcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.linknewdevice.impl.LinkNewMobileHandler
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.linknewdevice.LinkMobileStep
import io.element.android.libraries.matrix.api.logs.LoggerTags
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

private val tag = LoggerTag("ShowQrCodePresenter", LoggerTags.linkNewDevice)

@AssistedInject
class ShowQrCodePresenter(
    @Assisted private val initialData: String,
    private val linkNewMobileHandler: LinkNewMobileHandler,
) : Presenter<ShowQrCodeState> {
    @AssistedFactory
    interface Factory {
        fun create(initialData: String): ShowQrCodePresenter
    }

    private var loadingJob: Job? = null

    @Composable
    override fun present(): ShowQrCodeState {
        var qrCodeRotationCounter by remember { mutableIntStateOf(MAX_QR_CODE_ROTATION) }
        val state by produceState(
            initialValue = ShowQrCodeState(
                data1 = AsyncData.Success(initialData),
                data2 = AsyncData.Uninitialized,
                dataToRender = 1,
            )
        ) {
            linkNewMobileHandler.stepFlow.collect { step ->
                val currentValue = value
                when (step) {
                    is LinkMobileStep.QrReady -> {
                        loadingJob?.cancel()
                        if (currentValue.dataToRender == 1) {
                            value = currentValue.copy(
                                data2 = AsyncData.Success(step.data),
                                dataToRender = 2,
                            )
                        } else {
                            value = currentValue.copy(
                                data1 = AsyncData.Success(step.data),
                                dataToRender = 1,
                            )
                        }
                    }
                    is LinkMobileStep.QrRotating -> {
                        if (qrCodeRotationCounter-- > 0) {
                            Timber.tag(tag.value).d("Rotating QrCode")
                            linkNewMobileHandler.rotateQrCode()
                            // Ensure that outdated data is not rendered too long while rotating QR code
                            loadingJob = launch {
                                delay(1000)
                                if (currentValue.dataToRender == 1) {
                                    value = currentValue.copy(
                                        data2 = AsyncData.Loading(),
                                        dataToRender = 2,
                                    )
                                } else {
                                    value = currentValue.copy(
                                        data1 = AsyncData.Loading(),
                                        dataToRender = 1,
                                    )
                                }
                            }
                        } else {
                            Timber.tag(tag.value).w("Max QR code rotation reached, not rotating anymore")
                            linkNewMobileHandler.onTooManyRotation()
                        }
                    }
                    else -> Unit
                }
            }
        }

        return state
    }

    companion object {
        const val MAX_QR_CODE_ROTATION = 10
    }
}
