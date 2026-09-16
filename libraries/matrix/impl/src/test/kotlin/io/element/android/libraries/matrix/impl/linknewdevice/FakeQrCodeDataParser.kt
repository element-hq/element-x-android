/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.linknewdevice

import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiQrCodeData
import org.matrix.rustcomponents.sdk.QrCodeData

class FakeQrCodeDataParser : QrCodeDataParser {
    override fun parse(data: ByteArray): QrCodeData {
        return FakeFfiQrCodeData()
    }
}
