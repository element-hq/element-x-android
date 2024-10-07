/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
