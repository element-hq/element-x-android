/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.auth.qrlogin

import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginDataFactory
import io.element.android.tests.testutils.lambda.lambdaRecorder

class FakeMatrixQrCodeLoginDataFactory(
    var parseQrCodeLoginDataResult: () -> Result<MatrixQrCodeLoginData> =
        lambdaRecorder<Result<MatrixQrCodeLoginData>> { Result.success(FakeMatrixQrCodeLoginData()) },
) : MatrixQrCodeLoginDataFactory {
    override fun parseQrCodeData(data: ByteArray): Result<MatrixQrCodeLoginData> {
        return parseQrCodeLoginDataResult()
    }
}

class FakeMatrixQrCodeLoginData : MatrixQrCodeLoginData
