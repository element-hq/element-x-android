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
import org.matrix.rustcomponents.sdk.GrantQrLoginProgress
import org.matrix.rustcomponents.sdk.GrantQrLoginProgressListener
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.QrCodeData

class FakeFfiGrantLoginWithQrCodeHandler(
    private val generateResult: suspend () -> Unit = {},
    private val scanResult: suspend (QrCodeData) -> Unit = {},
) : GrantLoginWithQrCodeHandler(NoHandle) {
    private var generateProgressListener: GrantGeneratedQrLoginProgressListener? = null
    private var scanProgressListener: GrantQrLoginProgressListener? = null
    override suspend fun generate(progressListener: GrantGeneratedQrLoginProgressListener) {
        generateProgressListener = progressListener
        generateResult()
    }

    fun emitGenerateProgress(progress: GrantGeneratedQrLoginProgress) {
        generateProgressListener?.onUpdate(progress)
    }

    override suspend fun scan(qrCodeData: QrCodeData, progressListener: GrantQrLoginProgressListener) {
        scanProgressListener = progressListener
        scanResult(qrCodeData)
    }

    fun emitScanProgress(progress: GrantQrLoginProgress) {
        scanProgressListener?.onUpdate(progress)
    }
}
