/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.sessionstorage.api

// Imported from Element Android, to be able to migrate from EA to EXA.
enum class LoginType {
    PASSWORD,
    OIDC,
    SSO,
    UNSUPPORTED,
    CUSTOM,
    DIRECT,
    UNKNOWN,
    QR;

    companion object {
        fun fromName(name: String) = when (name) {
            PASSWORD.name -> PASSWORD
            OIDC.name -> OIDC
            SSO.name -> SSO
            UNSUPPORTED.name -> UNSUPPORTED
            CUSTOM.name -> CUSTOM
            DIRECT.name -> DIRECT
            QR.name -> QR
            else -> UNKNOWN
        }
    }
}
