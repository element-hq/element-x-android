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
