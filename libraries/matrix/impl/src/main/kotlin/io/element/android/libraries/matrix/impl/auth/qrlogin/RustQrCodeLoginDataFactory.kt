/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth.qrlogin

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginDataFactory
import org.matrix.rustcomponents.sdk.QrCodeData

@ContributesBinding(AppScope::class)
class RustQrCodeLoginDataFactory : MatrixQrCodeLoginDataFactory {
    override fun parseQrCodeData(data: ByteArray): Result<MatrixQrCodeLoginData> {
        return runCatchingExceptions { SdkQrCodeLoginData(QrCodeData.fromBytes(data)) }
    }
}
