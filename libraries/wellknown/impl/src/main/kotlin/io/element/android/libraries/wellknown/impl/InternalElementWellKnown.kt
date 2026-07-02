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
 *     "registration_helper_url": "https://element.io",
 *     "enforce_element_pro": true,
 *     "rageshake_url": "https://example.org/rageshake",
 *     "brand_color": "#FF0000",
 *     "notification_sound": "ring.flac",
 *     "idp_app_scheme": "io.element.app",
 *     "custom_recovery_passphrase": {
 *         "min_character_count": 8
 *     }
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
    @SerialName("notification_sound")
    val notificationSound: String? = null,
    @SerialName("idp_app_scheme")
    val identityProviderAppScheme: String? = null,
    @SerialName("custom_recovery_passphrase")
    val customRecoveryPassphrase: InternalCustomRecoveryPassphrase? = null,
    @SerialName("content_scanner_url")
    val contentScannerUrl: String? = null,
)

@Serializable
data class InternalCustomRecoveryPassphrase(
    @SerialName("min_character_count")
    val minCharacterCount: Int? = null,
)
