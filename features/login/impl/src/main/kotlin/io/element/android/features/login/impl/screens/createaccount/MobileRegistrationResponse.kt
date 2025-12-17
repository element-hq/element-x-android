/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.createaccount

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * For ref:
 * https://github.com/element-hq/matrix-react-sdk/pull/42/files#diff-2bbba5a742004fd4e924a639ded444279f66f7ad890cb669fbc91ac6b8638c64R56
 */
@Serializable
data class MobileRegistrationResponse(
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("home_server")
    val homeServer: String? = null,
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("device_id")
    val deviceId: String? = null,
)
