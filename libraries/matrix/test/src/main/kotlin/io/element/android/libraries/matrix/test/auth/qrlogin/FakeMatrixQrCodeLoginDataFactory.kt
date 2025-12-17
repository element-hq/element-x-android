/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.auth.qrlogin

import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginDataFactory
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder

class FakeMatrixQrCodeLoginDataFactory(
    var parseQrCodeLoginDataResult: () -> Result<MatrixQrCodeLoginData> =
        lambdaRecorder<Result<MatrixQrCodeLoginData>> { Result.success(FakeMatrixQrCodeLoginData()) },
) : MatrixQrCodeLoginDataFactory {
    override fun parseQrCodeData(data: ByteArray): Result<MatrixQrCodeLoginData> {
        return parseQrCodeLoginDataResult()
    }
}

class FakeMatrixQrCodeLoginData(
    private val serverNameResult: () -> String? = { lambdaError() },
) : MatrixQrCodeLoginData {
    override fun serverName() = serverNameResult()
}
