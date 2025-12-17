/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import org.matrix.rustcomponents.sdk.HomeserverLoginDetails

fun HomeserverLoginDetails.map(): MatrixHomeServerDetails = use {
    MatrixHomeServerDetails(
        url = url(),
        supportsPasswordLogin = supportsPasswordLogin(),
        supportsOidcLogin = supportsOidcLogin(),
    )
}
