/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oidc.api

sealed interface OidcAction {
    data class GoBack(val toUnblock: Boolean = false) : OidcAction
    data class Success(val url: String) : OidcAction
}
