/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

data class AccountProvider(
    val url: String,
    val title: String = url.removePrefix("https://").removePrefix("http://"),
    val subtitle: String? = null,
    val isPublic: Boolean = false,
    val isMatrixOrg: Boolean = false,
    val isValid: Boolean = false,
)
