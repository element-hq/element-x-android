/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.linknewdevice.ErrorType

open class LinkNewDeviceRootStateProvider : PreviewParameterProvider<LinkNewDeviceRootState> {
    override val values: Sequence<LinkNewDeviceRootState>
        get() = sequenceOf(
            aLinkNewDeviceRootState(),
            aLinkNewDeviceRootState(isSupported = AsyncData.Success(true)),
            aLinkNewDeviceRootState(isSupported = AsyncData.Success(false)),
            aLinkNewDeviceRootState(isSupported = AsyncData.Failure(Exception("Should not happen"))),
            aLinkNewDeviceRootState(
                isSupported = AsyncData.Success(true),
                qrCodeData = AsyncData.Loading(),
            ),
            aLinkNewDeviceRootState(
                isSupported = AsyncData.Success(true),
                qrCodeData = AsyncData.Failure(ErrorType.NotFound("The rendezvous session was not found and might have expired")),
            ),
        )
}

fun aLinkNewDeviceRootState(
    isSupported: AsyncData<Boolean> = AsyncData.Uninitialized,
    isPinConfigured: Boolean = false,
    qrCodeData: AsyncData<Unit> = AsyncData.Uninitialized,
    eventSink: (LinkNewDeviceRootEvent) -> Unit = { },
) = LinkNewDeviceRootState(
    isSupported = isSupported,
    isPinConfigured = isPinConfigured,
    qrCodeData = qrCodeData,
    eventSink = eventSink,
)
