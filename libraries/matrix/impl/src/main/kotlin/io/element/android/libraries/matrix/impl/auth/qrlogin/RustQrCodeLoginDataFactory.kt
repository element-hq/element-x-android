/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth.qrlogin

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginDataFactory
import org.matrix.rustcomponents.sdk.QrCodeData
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class RustQrCodeLoginDataFactory @Inject constructor() : MatrixQrCodeLoginDataFactory {
    override fun parseQrCodeData(data: ByteArray): Result<MatrixQrCodeLoginData> {
        return runCatchingExceptions { SdkQrCodeLoginData(QrCodeData.fromBytes(data)) }
    }
}
