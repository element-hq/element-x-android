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
import org.matrix.rustcomponents.sdk.HumanQrLoginError
import org.matrix.rustcomponents.sdk.qrLoginErrorToHumanError
import uniffi.matrix_sdk_crypto.LoginQrCodeDecodeError
import org.matrix.rustcomponents.sdk.QrCodeDecodeException as RustQrCodeDecodeException
import org.matrix.rustcomponents.sdk.QrLoginException as RustQrLoginException

object QrErrorMapper {
    fun map(loginException: RustQrLoginException) : QrLoginException = map(qrLoginErrorToHumanError(loginException))

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
    
    private fun map(humanQrLoginError: HumanQrLoginError): QrLoginException = when (humanQrLoginError) {
        is HumanQrLoginError.Cancelled -> QrLoginException.Cancelled
        is HumanQrLoginError.ConnectionInsecure -> QrLoginException.ConnectionInsecure
        is HumanQrLoginError.Declined -> QrLoginException.Declined
        is HumanQrLoginError.Expired -> QrLoginException.Expired
        is HumanQrLoginError.InvalidQrCode -> QrLoginException.InvalidQrCode
        is HumanQrLoginError.LinkingNotSupported -> QrLoginException.LinkingNotSupported
        is HumanQrLoginError.Unknown -> QrLoginException.Unknown
    }
}
