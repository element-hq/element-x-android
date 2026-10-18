/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.GeneratedQrLoginProgressListener
import org.matrix.rustcomponents.sdk.LoginWithQrCodeHandler
import org.matrix.rustcomponents.sdk.LoginWithQrCodeTask
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.QrCodeData
import org.matrix.rustcomponents.sdk.QrLoginProgress
import org.matrix.rustcomponents.sdk.QrLoginProgressListener

class FakeFfiLoginWithQrCodeHandler(
    private val generateResult: suspend () -> Unit = {},
    private val scanResult: suspend (QrCodeData) -> Unit = {},
    private val cancelResult: () -> Unit = {},
) : LoginWithQrCodeHandler(NoHandle) {
    private var scanProgressListener: QrLoginProgressListener? = null

    override fun generate(progressListener: GeneratedQrLoginProgressListener): LoginWithQrCodeTask {
        return FakeFfiLoginWithQrCodeTask(
            waitResult = generateResult,
            cancelResult = cancelResult,
        )
    }

    override fun scan(qrCodeData: QrCodeData, progressListener: QrLoginProgressListener): LoginWithQrCodeTask {
        scanProgressListener = progressListener
        return FakeFfiLoginWithQrCodeTask(
            waitResult = { scanResult(qrCodeData) },
            cancelResult = cancelResult,
        )
    }

    fun emitScanProgress(progress: QrLoginProgress) {
        scanProgressListener?.onUpdate(progress)
    }

    override fun close() = Unit
}
