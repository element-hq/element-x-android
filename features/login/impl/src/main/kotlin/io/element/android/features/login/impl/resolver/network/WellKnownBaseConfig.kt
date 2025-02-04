/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.resolver.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://matrix.org/docs/spec/client_server/r0.4.0.html#server-discovery
 * <pre>
 * {
 *     "base_url": "https://element.io"
 * }
 * </pre>
 * .
 */
@Serializable
data class WellKnownBaseConfig(
    @SerialName("base_url")
    val baseURL: String? = null
)
