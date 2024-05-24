/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.auth.qrlogin

import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeDecodeException
import io.element.android.libraries.matrix.api.auth.qrlogin.QrLoginException
import org.matrix.rustcomponents.sdk.HumanQrLoginException
import org.matrix.rustcomponents.sdk.HumanQrLoginException as RustHumanQrLoginException
import uniffi.matrix_sdk_crypto.LoginQrCodeDecodeError
import org.matrix.rustcomponents.sdk.QrCodeDecodeException as RustQrCodeDecodeException

object QrErrorMapper {
    fun map(qrCodeDecodeException: RustQrCodeDecodeException) : QrCodeDecodeException = when (qrCodeDecodeException) {
        is RustQrCodeDecodeException.Crypto -> {
            val reason = when (qrCodeDecodeException.error) {
                LoginQrCodeDecodeError.NOT_ENOUGH_DATA -> QrCodeDecodeException.Crypto.Reason.NOT_ENOUGH_DATA
                LoginQrCodeDecodeError.NOT_UTF8 -> QrCodeDecodeException.Crypto.Reason.NOT_UTF8
                LoginQrCodeDecodeError.URL_PARSE -> QrCodeDecodeException.Crypto.Reason.URL_PARSE
                LoginQrCodeDecodeError.INVALID_MODE -> QrCodeDecodeException.Crypto.Reason.INVALID_MODE
                LoginQrCodeDecodeError.INVALID_VERSION -> QrCodeDecodeException.Crypto.Reason.INVALID_VERSION
                LoginQrCodeDecodeError.BASE64 -> QrCodeDecodeException.Crypto.Reason.BASE64
                LoginQrCodeDecodeError.INVALID_PREFIX -> QrCodeDecodeException.Crypto.Reason.INVALID_PREFIX
            }
            QrCodeDecodeException.Crypto(qrCodeDecodeException.message, reason)
        }
    }
    
    fun map(humanQrLoginError: RustHumanQrLoginException): QrLoginException = when (humanQrLoginError) {
        is RustHumanQrLoginException.Cancelled -> QrLoginException.Cancelled
        is RustHumanQrLoginException.ConnectionInsecure -> QrLoginException.ConnectionInsecure
        is RustHumanQrLoginException.Declined -> QrLoginException.Declined
        is RustHumanQrLoginException.Expired -> QrLoginException.Expired
        is RustHumanQrLoginException.InvalidQrCode -> QrLoginException.InvalidQrCode
        is RustHumanQrLoginException.LinkingNotSupported -> QrLoginException.LinkingNotSupported
        is RustHumanQrLoginException.Unknown -> QrLoginException.Unknown
        is HumanQrLoginException.OidcMetadataInvalid -> QrLoginException.OidcMetadataInvalid
        is HumanQrLoginException.SlidingSyncNotAvailable -> QrLoginException.SlidingSyncNotAvailable
    }
}
