/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth.qrlogin

import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeDecodeException
import io.element.android.libraries.matrix.api.auth.qrlogin.QrLoginException
import org.matrix.rustcomponents.sdk.HumanQrLoginException as RustHumanQrLoginException
import org.matrix.rustcomponents.sdk.QrCodeDecodeException as RustQrCodeDecodeException

object QrErrorMapper {
    fun map(qrCodeDecodeException: RustQrCodeDecodeException): QrCodeDecodeException = when (qrCodeDecodeException) {
        is RustQrCodeDecodeException.Crypto -> {
            // We plan to restore it in the future when UniFFi can process them
//            val reason = when (qrCodeDecodeException.error) {
//                LoginQrCodeDecodeError.NOT_ENOUGH_DATA -> QrCodeDecodeException.Crypto.Reason.NOT_ENOUGH_DATA
//                LoginQrCodeDecodeError.NOT_UTF8 -> QrCodeDecodeException.Crypto.Reason.NOT_UTF8
//                LoginQrCodeDecodeError.URL_PARSE -> QrCodeDecodeException.Crypto.Reason.URL_PARSE
//                LoginQrCodeDecodeError.INVALID_MODE -> QrCodeDecodeException.Crypto.Reason.INVALID_MODE
//                LoginQrCodeDecodeError.INVALID_VERSION -> QrCodeDecodeException.Crypto.Reason.INVALID_VERSION
//                LoginQrCodeDecodeError.BASE64 -> QrCodeDecodeException.Crypto.Reason.BASE64
//                LoginQrCodeDecodeError.INVALID_PREFIX -> QrCodeDecodeException.Crypto.Reason.INVALID_PREFIX
//            }
            QrCodeDecodeException.Crypto(
                qrCodeDecodeException.message.orEmpty(),
//                reason
            )
        }
    }

    fun map(humanQrLoginError: RustHumanQrLoginException): QrLoginException = when (humanQrLoginError) {
        is RustHumanQrLoginException.Cancelled -> QrLoginException.Cancelled
        is RustHumanQrLoginException.ConnectionInsecure -> QrLoginException.ConnectionInsecure
        is RustHumanQrLoginException.Declined -> QrLoginException.Declined
        is RustHumanQrLoginException.Expired -> QrLoginException.Expired
        is RustHumanQrLoginException.OtherDeviceNotSignedIn -> QrLoginException.OtherDeviceNotSignedIn
        is RustHumanQrLoginException.LinkingNotSupported -> QrLoginException.LinkingNotSupported
        is RustHumanQrLoginException.Unknown -> QrLoginException.Unknown
        is RustHumanQrLoginException.OidcMetadataInvalid -> QrLoginException.OidcMetadataInvalid
        is RustHumanQrLoginException.SlidingSyncNotAvailable -> QrLoginException.SlidingSyncNotAvailable
        is RustHumanQrLoginException.CheckCodeAlreadySent -> QrLoginException.CheckCodeAlreadySent
        is RustHumanQrLoginException.CheckCodeCannotBeSent -> QrLoginException.CheckCodeCannotBeSent
        is RustHumanQrLoginException.NotFound -> QrLoginException.NotFound
    }
}
