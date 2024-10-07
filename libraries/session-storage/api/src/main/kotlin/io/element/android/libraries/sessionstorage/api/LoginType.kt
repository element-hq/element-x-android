/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
