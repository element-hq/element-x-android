/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth.qrlogin

/**
 * Decodes the raw bytes of a scanned QR code into login data the SDK can use.
 */
interface MatrixQrCodeLoginDataFactory {
    /**
     * Parses the scanned bytes, failing with a [QrCodeDecodeException] when they are not a valid Matrix login QR code.
     *
     * @param data the raw content read from the QR code.
     */
    fun parseQrCodeData(data: ByteArray): Result<MatrixQrCodeLoginData>
}
