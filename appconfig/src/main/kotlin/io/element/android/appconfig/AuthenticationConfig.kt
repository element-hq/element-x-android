/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appconfig

object AuthenticationConfig {
    // Alpha local Synapse — host LAN IP works from AVD, LDPlayer, and physical
    // device on same Wi-Fi. Synapse's `public_baseurl` must match this exact URL,
    // because the Rust SDK follows the base_url returned by /.well-known/matrix/client.
    const val MATRIX_ORG_URL = "http://192.168.1.65:8008"

    /**
     * URL with some docs that explain what's sliding sync and how to add it to your home server.
     */
    const val SLIDING_SYNC_READ_MORE_URL = "https://github.com/matrix-org/sliding-sync/blob/main/docs/Landing.md"

    /**
     * Force a sliding sync proxy url, if not null, the proxy url in the .well-known file will be ignored.
     */
    val SLIDING_SYNC_PROXY_URL: String? = null
}
