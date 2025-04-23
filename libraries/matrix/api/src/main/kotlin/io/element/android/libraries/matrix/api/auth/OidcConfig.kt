/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

import io.element.android.libraries.matrix.api.BuildConfig

object OidcConfig {
    const val CLIENT_URI = BuildConfig.CLIENT_URI

    // Notes:
    // 1. the scheme must match the value declared in the AndroidManifest.xml
    // 2. the scheme must be the reverse of the host of CLIENT_URI
    const val REDIRECT_URI = BuildConfig.REDIRECT_URI

    // Note: host must match with the host of CLIENT_URI
    const val LOGO_URI = BuildConfig.LOGO_URI

    // Note: host must match with the host of CLIENT_URI
    const val TOS_URI = BuildConfig.TOS_URI

    // Note: host must match with the host of CLIENT_URI
    const val POLICY_URI = BuildConfig.POLICY_URI

    // Some homeservers/auth issuers don't support dynamic client registration, and have to be registered manually
    val STATIC_REGISTRATIONS = mapOf(
        "https://id.thirdroom.io/realms/thirdroom" to "elementx",
    )
}
