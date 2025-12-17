/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.auth

import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL

fun aMatrixHomeServerDetails(
    url: String = A_HOMESERVER_URL,
    supportsPasswordLogin: Boolean = false,
    supportsOidcLogin: Boolean = false,
) = MatrixHomeServerDetails(
    url = url,
    supportsPasswordLogin = supportsPasswordLogin,
    supportsOidcLogin = supportsOidcLogin,
)
