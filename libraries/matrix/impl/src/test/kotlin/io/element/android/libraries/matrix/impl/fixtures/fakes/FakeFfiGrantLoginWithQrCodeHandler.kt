/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.GrantGeneratedQrLoginProgress
import org.matrix.rustcomponents.sdk.GrantGeneratedQrLoginProgressListener
import org.matrix.rustcomponents.sdk.GrantLoginWithQrCodeHandler
import org.matrix.rustcomponents.sdk.GrantLoginWithQrCodeTask
import org.matrix.rustcomponents.sdk.GrantQrLoginProgress
import org.matrix.rustcomponents.sdk.GrantQrLoginProgressListener
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.QrCodeData

class FakeFfiGrantLoginWithQrCodeHandler(
    private val generateResult: suspend () -> Unit = {},
    private val scanResult: suspend (QrCodeData) -> Unit = {},
    private val cancelResult: () -> Unit = {},
) : GrantLoginWithQrCodeHandler(NoHandle) {
    private var generateProgressListener: GrantGeneratedQrLoginProgressListener? = null
    private var scanProgressListener: GrantQrLoginProgressListener? = null
    override fun generate(progressListener: GrantGeneratedQrLoginProgressListener): GrantLoginWithQrCodeTask {
        generateProgressListener = progressListener
        return FakeFfiGrantLoginWithQrCodeTask(
            waitResult = generateResult,
            cancelResult = cancelResult,
        )
    }

    fun emitGenerateProgress(progress: GrantGeneratedQrLoginProgress) {
        generateProgressListener?.onUpdate(progress)
    }

    override fun scan(qrCodeData: QrCodeData, progressListener: GrantQrLoginProgressListener): GrantLoginWithQrCodeTask {
        scanProgressListener = progressListener
        return FakeFfiGrantLoginWithQrCodeTask(
            waitResult = { scanResult(qrCodeData) },
            cancelResult = cancelResult,
        )
    }

    fun emitScanProgress(progress: GrantQrLoginProgress) {
        scanProgressListener?.onUpdate(progress)
    }
}
