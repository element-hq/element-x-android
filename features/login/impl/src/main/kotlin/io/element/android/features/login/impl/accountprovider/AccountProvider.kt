/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import io.element.android.appconfig.AuthenticationConfig

data class AccountProvider(
    val url: String,
    val title: String = url.removePrefix("https://").removePrefix("http://"),
    val subtitleResourceId: Int? = null,
    val isPublic: Boolean = false,
) {
    fun isMatrixOrg(): Boolean {
        return url == AuthenticationConfig.MATRIX_ORG_URL
    }
}
