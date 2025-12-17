/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

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
data class InternalElementWellKnown(
    @SerialName("registration_helper_url")
    val registrationHelperUrl: String? = null,
    @SerialName("enforce_element_pro")
    val enforceElementPro: Boolean? = null,
    @SerialName("rageshake_url")
    val rageshakeUrl: String? = null,
    @SerialName("brand_color")
    val brandColor: String? = null,
)
