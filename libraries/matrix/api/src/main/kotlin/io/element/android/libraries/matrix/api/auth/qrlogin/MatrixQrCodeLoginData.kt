/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth.qrlogin

/**
 * The decoded content of a login QR code, as produced by [MatrixQrCodeLoginDataFactory].
 *
 * The underlying data is opaque to the app: it is passed back to the SDK to perform the login.
 */
interface MatrixQrCodeLoginData {
    /** The homeserver advertised by the QR code, or `null` when it does not carry one. */
    fun serverName(): String?
}
