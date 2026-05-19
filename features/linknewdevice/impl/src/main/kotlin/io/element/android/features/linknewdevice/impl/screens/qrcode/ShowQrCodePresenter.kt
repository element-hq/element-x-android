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

    @Composable
    override fun present(): ShowQrCodeState {
        var qrCodeRotationCounter by remember { mutableIntStateOf(MAX_QR_CODE_ROTATION) }
        val data by produceState<AsyncData<String>>(AsyncData.Success(initialData)) {
            linkNewMobileHandler.stepFlow.collect { step ->
                when (step) {
                    is LinkMobileStep.QrReady -> {
                        value = AsyncData.Success(step.data)
                    }
                    is LinkMobileStep.QrRotating -> {
                        if (qrCodeRotationCounter-- > 0) {
                            Timber.tag(tag.value).d("Rotating QrCode")
                            linkNewMobileHandler.rotateQrCode()
                            value = AsyncData.Loading()
                        } else {
                            Timber.tag(tag.value).w("Max QR code rotation reached, not rotating anymore")
                            linkNewMobileHandler.onTooManyRotation()
                        }
                    }
                    else -> Unit
                }
            }
        }

        return ShowQrCodeState(
            data = data,
        )
    }

    companion object {
        const val MAX_QR_CODE_ROTATION = 10
    }
}
