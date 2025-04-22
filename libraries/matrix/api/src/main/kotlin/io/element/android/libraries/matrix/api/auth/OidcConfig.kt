/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

object OidcConfig {
    const val CLIENT_URI = "https://element.io"

    // Notes:
    // 1. the scheme must match the value declared in the AndroidManifest.xml
    // 2. the scheme must be the reverse of the host of CLIENT_URI
    const val REDIRECT_URI = "io.element:/callback"

    // Note: host must match with the host of CLIENT_URI
    const val LOGO_URI = "https://element.io/mobile-icon.png"

    // Note: host must match with the host of CLIENT_URI
    const val TOS_URI = "https://element.io/acceptable-use-policy-terms"

    // Note: host must match with the host of CLIENT_URI
    const val POLICY_URI = "https://element.io/privacy"

    const val CONTACT = "support@element.io"

    // Some homeservers/auth issuers don't support dynamic client registration, and have to be registered manually
    val STATIC_REGISTRATIONS = mapOf(
        "https://id.thirdroom.io/realms/thirdroom" to "elementx",
    )
}
