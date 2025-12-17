/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth.qrlogin

sealed class QrCodeDecodeException(message: String) : Exception(message) {
    class Crypto(
        message: String,
//        val reason: Reason
    ) : QrCodeDecodeException(message) {
        // We plan to restore it in the future when UniFFi can process them
//        enum class Reason {
//            NOT_ENOUGH_DATA,
//            NOT_UTF8,
//            URL_PARSE,
//            INVALID_MODE,
//            INVALID_VERSION,
//            BASE64,
//            INVALID_PREFIX
//        }
    }
}
