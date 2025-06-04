/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.tests.testutils.lambda.lambdaError
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.QrCodeData

class FakeFfiQrCodeData(
    private val serverNameResult: () -> String? = { lambdaError() },
) : QrCodeData(NoPointer) {
    override fun serverName(): String? {
        return serverNameResult()
    }
}
