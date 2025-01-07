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
 * Example:
 * <pre>
 * {
 *     "registration_helper_url": "https://element.io"
 * }
 * </pre>
 * .
 */
@Serializable
data class ElementWellKnown(
    @SerialName("registration_helper_url")
    val registrationHelperUrl: String? = null,
)
