/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

import io.element.android.libraries.matrix.api.BuildConfig

object OAuthConfig {
    // This *can* be null if the build time config field is not set
    @Suppress("UNNECESSARY_SAFE_CALL")
    val CLIENT_URI = BuildConfig.OAUTH_CLIENT_URI_PATH?.let {
        // Concatenate the base URI and the path, ensuring there is exactly one '/' between them
        "${BuildConfig.CLIENT_URI.trimEnd('/')}/${it.trimStart('/')}"
    } ?: BuildConfig.CLIENT_URI

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
