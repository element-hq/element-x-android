/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
